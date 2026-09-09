package pf.cyj.sys.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.entity.AnlCol;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.DmnPdt;
import pf.cyj.sys.exception.ResourceNotFoundException;
import pf.cyj.sys.repository.AnlColRepository;
import pf.cyj.sys.repository.AnlDsetRepository;
import pf.cyj.sys.repository.DmnPdtRepository;

/**
 * 데이터셋 단위 AI 판별결과 PDF 리포트 생성 (Apache PDFBox 3.0.5). AnlDsetColController 의
 * GET /api/datasets/{datasetId}/report.pdf 가 이 클래스를 호출한다.
 *
 * <p>PDFBox 내장 표준 폰트(Helvetica 등)는 한글 글리프가 없어서 한글이 전부 깨지거나 빈 칸으로
 * 나온다. 그래서 {@code src/main/resources/fonts/NotoSansKR-Regular.ttf}를 PDType0Font로 임베드해서
 * 쓴다 - 배포 서버에 별도로 한글 폰트를 설치하지 않아도 항상 동일하게 렌더링되는 게 핵심이다. 이 폰트는
 * 구글의 Noto Sans CJK KR Regular(SIL Open Font License)에서 한글 완성형/영문/숫자/기본 문장부호
 * 글리프만 추려 서브셋(fonttools pyftsubset)한 것이라 원본(약 16MB)보다 훨씬 가볍다(약 1.9MB).
 */
@Service
@RequiredArgsConstructor
public class PdfReportService {

    private static final String FONT_PATH = "fonts/NotoSansKR-Regular.ttf";
    private static final float MARGIN = 50f;
    private static final float TITLE_SIZE = 18f;
    private static final float SECTION_SIZE = 12f;
    private static final float BODY_SIZE = 10.5f;
    private static final float LINE_HEIGHT = 16f;

    private final AnlDsetRepository anlDsetRepository;
    private final AnlColRepository anlColRepository;
    private final DmnPdtRepository dmnPdtRepository;

    /**
     * 지정한 데이터셋의 컬럼별 AI 판별 결과(Top-N)를 담은 PDF를 생성해 바이트 배열로 반환한다.
     *
     * <p>(버그 수정) 이 메서드에 원래 @Transactional 이 없었다 - Spring Data JPA의 각 리포지토리 메서드
     * (findById, findByAnlDset_DatasetId, findByAnlCol_ColumnIdOrderByPredictionRankAsc)는 자기
     * 자신만의 @Transactional(readOnly=true)로 실행되고 메서드가 끝나는 즉시 그 트랜잭션(Hibernate
     * 세션)이 닫힌다. DmnPdt.dmnCd 는 FetchType.LAZY 라서, 판별 결과가 있는 컬럼을 만나 아래 반복문에서
     * p.getDmnCd().getDomainNameKo() 를 호출하는 시점엔 이미 그 조회에 쓰인 세션이 닫혀 있어
     * org.hibernate.LazyInitializationException("could not initialize proxy - no Session")이
     * 발생했다 - GlobalExceptionHandler의 handleUnexpected()로 떨어져 500 에러가 나면서 PDF 다운로드
     * 자체가 실패했다(판별 결과가 없는 컬럼만 있는 데이터셋은 이 지연 로딩을 안 건드리므로 우연히
     * 성공했다 - "PDF 생성 안되는거같은데,,?"라는 증상과 정확히 일치한다). 메서드 전체를 하나의
     * 읽기전용 트랜잭션(세션)으로 묶어, 조회와 지연 로딩 접근이 같은 세션 안에서 이뤄지게 한다.
     */
    @Transactional(readOnly = true)
    public byte[] generateDatasetReport(String datasetId) {
        AnlDset dset = anlDsetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 데이터셋입니다: " + datasetId));
        List<AnlCol> columns = anlColRepository.findByAnlDset_DatasetId(datasetId);

        try (PDDocument document = new PDDocument()) {
            PDFont font = loadFont(document);

            // 여러 페이지에 걸쳐 텍스트를 이어 쓸 때 매번 새로 만들어야 하는 상태(현재 페이지의
            // ContentStream, 다음 줄을 쓸 y좌표)를 담는 그릇이다. PDFBox 는 한 페이지 안에서만
            // 텍스트를 이어 쓸 수 있고 페이지가 넘어가면 ContentStream 자체를 새로 열어야 해서,
            // Service 는 싱글턴 빈이라 이 값들을 필드로 두면 동시 요청끼리 값이 섞이니(스레드 안전
            // 하지 않으니) 메서드 로컬 상태로만 다룬다.
            PdfCursor cursor = new PdfCursor(document, font);
            cursor.startNewPage();

            cursor.writeLine("표준 도메인 판별 결과 리포트", TITLE_SIZE);
            cursor.writeLine(" ", BODY_SIZE);
            cursor.writeLine("데이터셋명: " + dset.getDatasetName(), BODY_SIZE);
            cursor.writeLine("대상 테이블: " + dset.getDbSchemaName() + "." + dset.getTableName()
                    + " (" + dset.getDbmsTypeCode() + ")", BODY_SIZE);
            cursor.writeLine("생성일시: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), BODY_SIZE);
            cursor.writeLine(" ", BODY_SIZE);

            if (columns.isEmpty()) {
                cursor.writeLine("등록된 컬럼이 없습니다.", BODY_SIZE);
            }

            for (AnlCol col : columns) {
                String colTitle = col.getColumnName()
                        + (col.getColumnNameKo() != null ? " (" + col.getColumnNameKo() + ")" : "");
                // (버그 수정) 원래 "■ "(U+25A0, BLACK SQUARE) 접두어를 썼는데, 이 폰트는 한글
                // 완성형·영문·숫자·기본 문장부호 글리프만 남긴 서브셋이라 도형 기호(■) 글리프가
                // 아예 없다 - PDPageContentStream.showText()가 글리프 없는 코드포인트를 만나면
                // "could not find the glyphId for the character" IOException을 던진다(제목/
                // 데이터셋 정보 줄까지는 전부 정상 출력되다가, 첫 컬럼 섹션 줄에서만 100% 재현되는
                // 이유). 폰트에 반드시 있는 ASCII 문장부호(#)로 바꿨다 - 도형 기호를 계속 쓰려면
                // 폰트 서브셋 자체를 U+25A0 포함해서 다시 만들어야 한다.
                cursor.writeLine("# " + colTitle, SECTION_SIZE);

                List<DmnPdt> predictions =
                        dmnPdtRepository.findByAnlCol_ColumnIdOrderByPredictionRankAsc(col.getColumnId());
                if (predictions.isEmpty()) {
                    cursor.writeLine("     - AI 판별 결과 없음", BODY_SIZE);
                } else {
                    for (DmnPdt p : predictions) {
                        String pct = p.getProbability() != null
                                ? p.getProbability().multiply(BigDecimal.valueOf(100))
                                        .setScale(1, RoundingMode.HALF_UP) + "%"
                                : "-";
                        cursor.writeLine("     " + p.getPredictionRank() + "순위: " + p.getDmnCd().getDomainNameKo()
                                + " (" + pct + ", " + p.getAiModelName() + ")", BODY_SIZE);
                    }
                }
                cursor.writeLine(" ", BODY_SIZE);
            }

            cursor.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("PDF 리포트 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * (버그 수정 - 이력) {@code NotoSansKR-Regular.ttf}는 확장자만 .ttf이고 실제로는 OpenType/CFF
     * 외곽선 폰트다(구글 Noto Sans CJK KR 원본 자체가 CFF 방식이라, pyftsubset으로 서브셋해도 외곽선
     * 형식은 그대로 유지된다 - 파일 앞 4바이트가 TrueType을 뜻하는 "\x00\x01\x00\x00"이 아니라
     * OpenType/CFF를 뜻하는 "OTTO"다). 이 폰트를 PDFBox에 물리는 과정에서 순서대로 두 가지 문제가
     * 있었다.
     *
     * <ol>
     *   <li><b>로딩 단계</b>: {@code PDType0Font.load(document, InputStream)}(스트림 하나만 받는
     *       버전)은 무조건 TrueType(glyf 외곽선) 전용 임베더(PDCIDFontType2Embedder)로 처리해서
     *       "True Type fonts using CFF outlines are not supported" 예외가 났다. → {@link OTFParser}
     *       로 직접 파싱해서 {@code OpenTypeFont}(TrueTypeFont 하위 타입)로 인식시키고,
     *       {@code TrueTypeFont}를 받는 {@code PDType0Font.load(document, TrueTypeFont, embedSubset)}
     *       오버로드를 쓰도록 고쳤다 - CFF 외곽선을 지원하는 PDCIDFontType0Embedder가 대신 선택된다.</li>
     *   <li><b>서브셋 단계(이번에 새로 발견)</b>: 위 오버로드에 {@code embedSubset=true}를 주면,
     *       PDFBox가 {@code document.save()} 시점에 "실제 PDF에 쓰인 글자만 남기고 나머지 글리프는
     *       버리는" 서브셋팅을 시도한다. 그런데 PDFBox/FontBox 3.0.5의 서브셋 구현({@code TTFSubsetter})
     *       은 TrueType(glyf 외곽선) 전용이라 내부적으로 {@code ttf.getGlyph(...)}를 호출하는데,
     *       CFF 외곽선 폰트({@code OpenTypeFont})는 애초에 glyf 테이블이 없어서
     *       {@code getGlyph()}가 {@code UnsupportedOperationException("OTF fonts do not have a
     *       glyf table")}을 던진다 - 이 예외는 {@code IOException}이 아니라서 아래 catch 블록에도
     *       안 걸리고 처리되지 않은 예외로 떨어졌다(FontBox/PDFBox가 CFF 폰트 서브셋팅 자체를 지원하지
     *       않는 한계다). → {@code embedSubset=false}로 바꿔서 서브셋을 아예 시도하지 않고, 이미
     *       한글 완성형/영문/숫자/기본 문장부호로 한 번 추려둔 폰트(약 1.9MB) 전체를 그대로 임베드하게
     *       했다 - PDF 하나당 폰트 용량이 조금 늘긴 하지만(실제 쓰인 글자만 남기는 추가 축소가 없을
     *       뿐), 서브셋팅 실패로 다운로드 자체가 안 되는 것보다는 훨씬 낫다.</li>
     * </ol>
     */
    private PDFont loadFont(PDDocument document) throws IOException {
        try (InputStream is = new ClassPathResource(FONT_PATH).getInputStream()) {
            TrueTypeFont otf = new OTFParser().parse(new RandomAccessReadBuffer(is));
            return PDType0Font.load(document, otf, false);
        }
    }

    /**
     * A4 페이지에 한 줄씩 텍스트를 써 내려가다 페이지 하단 여백에 닿으면 자동으로 새 페이지를 열어주는
     * 최소한의 도우미. 이 리포트 생성 메서드 하나에서만 쓰는 구현 세부사항이라 별도 파일로 빼지 않고
     * private static 중첩 클래스로 뒀다.
     */
    private static final class PdfCursor {
        private final PDDocument document;
        private final PDFont font;
        private PDPageContentStream contentStream;
        private float y;

        PdfCursor(PDDocument document, PDFont font) {
            this.document = document;
            this.font = font;
        }

        void startNewPage() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            y = PDRectangle.A4.getHeight() - MARGIN;
        }

        void writeLine(String text, float fontSize) throws IOException {
            if (y < MARGIN) {
                startNewPage();
            }
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(MARGIN, y);
            contentStream.showText(text);
            contentStream.endText();
            y -= LINE_HEIGHT;
        }

        void close() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
        }
    }
}