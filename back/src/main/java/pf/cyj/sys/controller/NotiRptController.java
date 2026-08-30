package pf.cyj.sys.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import pf.cyj.sys.dto.request.NotiLogCreateReq;
import pf.cyj.sys.dto.request.RptExpLogCreateReq;
import pf.cyj.sys.dto.response.NotiLogRsp;
import pf.cyj.sys.dto.response.RptExpLogRsp;
import pf.cyj.sys.oauth2.CustomOAuth2User;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.service.NotiRptService;

/**
 * 알림/리포트 - 알림은 실제 발송(이메일)까지 하고 이력을 남긴다. 리포트 이력 조회/수동 기록은
 * 여기서 하고, 실제 PDF 생성/다운로드는 AnlDsetColController(GET /api/datasets/{datasetId}/report.pdf,
 * PdfReportService, v4.8)가 담당하며 다운로드와 동시에 이력도 자동으로 남는다. 전부 로그인이 필요하다.
 * 조회는 본인 이력만 노출한다(findNotificationsByUser 를 ActorContext 의 getUserId() 로만 호출 -
 * 다른 사용자 ID 를 파라미터로 받지 않아 IDOR 을 원천 차단).
 */
@Tag(name = "NotificationReport", description = "알림/리포트 - 알림 발송(이메일) 및 리포트 출력 이력 조회·수동기록")
@RestController
@RequiredArgsConstructor
public class NotiRptController {

    private final NotiRptService notiRptService;

    @Operation(
            summary = "알림 발송 (실제 이메일 발송)",
            description = "지정한 사용자의 email로 실제 이메일을 발송하고 그 결과를 이력으로 남긴다. "
                    + "대상 사용자에게 email이 없거나 메일 서버 오류가 나면 발송은 건너뛰고 SEND_STATUS=FAIL로 "
                    + "기록된다(발송 실패가 500 에러로 이어지지는 않는다)."
    )
    @PostMapping("/api/notifications")
    public ResponseEntity<NotiLogRsp> recordNotification(@Valid @RequestBody NotiLogCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notiRptService.recordNotification(req));
    }

    @Operation(summary = "내 알림 이력 조회", description = "현재 로그인한 사용자에게 발송된 알림 이력만 최신순으로 조회한다(IDOR 방지를 위해 다른 사용자 ID 는 파라미터로 받지 않는다).")
    @GetMapping("/api/notifications/mine")
    public ResponseEntity<List<NotiLogRsp>> findMyNotifications(@AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.ok(notiRptService.findNotificationsByUser(actor.getUserId()));
    }

    @Operation(
            summary = "리포트 출력 이력 수동 기록",
            description = "리포트 출력/내보내기 이력을 수동으로 기록한다. 실제 PDF를 생성해서 받으려면 이 API 대신 "
                    + "GET /api/datasets/{datasetId}/report.pdf 를 호출한다 - 그쪽은 PDF 다운로드와 이력 기록을 "
                    + "한 번에 처리하므로, 이 API 는 외부에서 이미 만들어진 리포트를 내보냈다는 이력만 남기고 싶을 때 쓴다."
    )
    @PostMapping("/api/report-exports")
    public ResponseEntity<RptExpLogRsp> recordReportExport(@Valid @RequestBody RptExpLogCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notiRptService.recordReportExport(req));
    }

    @Operation(summary = "데이터셋별 리포트 출력 이력 조회", description = "지정한 데이터셋에 대해 기록된 리포트 출력 이력 목록을 최신순으로 조회한다.")
    @GetMapping("/api/report-exports/dataset/{datasetId}")
    public ResponseEntity<List<RptExpLogRsp>> findReportsByDataset(
            @Parameter(description = "데이터셋 ID", example = "DS0000000001")
            @PathVariable String datasetId) {
        return ResponseEntity.ok(notiRptService.findReportsByDataset(datasetId));
    }
}
