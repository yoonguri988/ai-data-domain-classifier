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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
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

    /** 지정한 데이터셋의 컬럼별 AI 판별 결과(Top-N)를 담은 PDF를 생성해 바이트 배열로 반환한다. */
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
                cursor.writeLine("■ " + colTitle, SECTION_SIZE);

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

    private PDFont loadFont(PDDocument document) throws IOException {
        try (InputStream is = new ClassPathResource(FONT_PATH).getInputStream()) {
            return PDType0Font.load(document, is);
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
