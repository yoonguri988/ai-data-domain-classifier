package pf.cyj.sys.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import pf.cyj.sys.dto.request.AnlColBulkCreateReq;
import pf.cyj.sys.dto.request.AnlDsetCreateReq;
import pf.cyj.sys.dto.request.RptExpLogCreateReq;
import pf.cyj.sys.dto.response.AnlColRsp;
import pf.cyj.sys.dto.response.AnlDsetRsp;
import pf.cyj.sys.oauth2.CustomOAuth2User;
import pf.cyj.sys.report.PdfReportService;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.service.AnlDsetColService;
import pf.cyj.sys.service.NotiRptService;

/**
 * 분석대상 - 데이터셋 등록/조회, 컬럼 메타 일괄 등록/조회, AI 판별결과 PDF 리포트 다운로드
 * (PdfReportService, v4.8). 전부 로그인이 필요하다(SecurityConfig 의 anyRequest().authenticated()
 * 기본값을 그대로 따른다). "/mine" 은 "/{datasetId}" 보다 먼저 선언하지 않아도 Spring 이 리터럴
 * 경로를 변수 경로보다 우선 매칭하므로 순서와 무관하게 정상 동작한다.
 *
 * <p>Swagger UI 테스트 순서 예시: (1) POST /api/datasets 로 데이터셋 등록 → 응답의 datasetId 확인
 * → (2) POST /api/datasets/columns 요청 바디의 datasetId 에 그 값을 넣어 컬럼 등록
 * → (3) GET /api/datasets/{datasetId}/report.pdf 로 PDF 다운로드(Swagger UI에서는 "Download file"
 * 링크로 받을 수 있다 - 컬럼별 AI 판별 결과가 아직 없어도(POST /api/columns/{columnId}/predict 를
 * 안 거쳤어도) "AI 판별 결과 없음"으로 표시되며 정상 생성된다).
 */
@Tag(name = "AnalysisTarget", description = "분석대상 - 데이터셋/컬럼 메타 등록·조회 및 AI 판별결과 PDF 리포트 다운로드 (로그인 필요)")
@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class AnlDsetColController {

    private final AnlDsetColService anlDsetColService;
    private final PdfReportService pdfReportService;
    private final NotiRptService notiRptService;

    @Operation(
            summary = "데이터셋 등록",
            description = "분석 대상 데이터셋을 신규 등록한다. 등록자는 인증 정보(로그인한 사용자)에서 자동으로 채워지고, "
                    + "datasetId/requestNo(업무키)는 서버가 자동 채번하므로 요청 바디에 넣지 않는다."
    )
    @PostMapping
    public ResponseEntity<AnlDsetRsp> createDset(
            @Valid @RequestBody AnlDsetCreateReq req,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(anlDsetColService.createDset(req, actor.getUserId()));
    }

    @Operation(summary = "내 데이터셋 목록 조회", description = "현재 로그인한 사용자가 요청한 데이터셋 목록을 조회한다.")
    @GetMapping("/mine")
    public ResponseEntity<List<AnlDsetRsp>> findMyDsets(@AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.ok(anlDsetColService.findDsetByRequester(actor.getUserId()));
    }

    @Operation(summary = "데이터셋 단건 조회", description = "데이터셋 ID(업무키)로 단건 상세를 조회한다.")
    @GetMapping("/{datasetId}")
    public ResponseEntity<AnlDsetRsp> findDsetById(
            @Parameter(description = "데이터셋 ID (데이터셋 등록 응답에서 받은 값)", example = "DS0000000001")
            @PathVariable("datasetId") String datasetId) {
        return ResponseEntity.ok(anlDsetColService.findDsetById(datasetId));
    }

    @Operation(
            summary = "컬럼 메타 일괄 등록",
            description = "하나의 데이터셋에 속한 컬럼 메타 정보를 여러 건 한 번에 등록한다(테이블 메타 스캔 결과 저장용). "
                    + "요청 바디의 datasetId 는 먼저 등록해 둔 데이터셋의 ID 여야 한다."
    )
    @PostMapping("/columns")
    public ResponseEntity<List<AnlColRsp>> createColumnsBulk(@Valid @RequestBody AnlColBulkCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anlDsetColService.createColumnsBulk(req));
    }

    @Operation(summary = "데이터셋별 컬럼 목록 조회", description = "지정한 데이터셋에 등록된 컬럼 메타 목록을 조회한다.")
    @GetMapping("/{datasetId}/columns")
    public ResponseEntity<List<AnlColRsp>> findColumnsByDataset(
            @Parameter(description = "데이터셋 ID", example = "DS0000000001")
            @PathVariable("datasetId") String datasetId) {
        return ResponseEntity.ok(anlDsetColService.findColumnsByDataset(datasetId));
    }

    @Operation(
            summary = "AI 판별결과 PDF 리포트 다운로드",
            description = "지정한 데이터셋의 컬럼별 AI 판별 결과(Top-N)를 PDFBox로 생성해 바로 내려받는다. "
                    + "다운로드 즉시 REPORT_EXPORT_LOG 에 출력 이력이 함께 기록된다(NotiRptService.recordReportExport)."
    )
    @GetMapping("/{datasetId}/report.pdf")
    public ResponseEntity<byte[]> downloadReport(
            @Parameter(description = "데이터셋 ID", example = "DS0000000001")
            @PathVariable("datasetId") String datasetId,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        byte[] pdf = pdfReportService.generateDatasetReport(datasetId);

        String fileName = datasetId + "_report.pdf";
        notiRptService.recordReportExport(new RptExpLogCreateReq(datasetId, actor.getUserId(), fileName));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
