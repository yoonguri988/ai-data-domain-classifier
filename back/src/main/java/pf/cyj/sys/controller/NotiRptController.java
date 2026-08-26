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
 * 알림/리포트 - 발송/출력 이력 기록 및 조회. 전부 로그인이 필요하다.
 * 조회는 본인 이력만 노출한다(findNotificationsByUser 를 ActorContext 의 getUserId() 로만 호출 -
 * 다른 사용자 ID 를 파라미터로 받지 않아 IDOR 을 원천 차단). 실제 발송(coolsms/mail)과
 * PDF 생성(PDFBox)은 NotiRptService 문서와 동일하게 외부 연동 단계에서 붙는다.
 */
@Tag(name = "NotificationReport", description = "알림/리포트 - 발송/출력 이력 기록 및 본인 이력 조회")
@RestController
@RequiredArgsConstructor
public class NotiRptController {

    private final NotiRptService notiRptService;

    @Operation(summary = "알림 발송 이력 기록", description = "알림(문자/메일 등) 발송 이력을 기록한다. 실제 발송 연동은 별도 단계에서 붙는다.")
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

    @Operation(summary = "리포트 출력 이력 기록", description = "리포트(PDF 등) 출력/내보내기 이력을 기록한다. 실제 PDF 생성 연동은 별도 단계에서 붙는다.")
    @PostMapping("/api/report-exports")
    public ResponseEntity<RptExpLogRsp> recordReportExport(@Valid @RequestBody RptExpLogCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notiRptService.recordReportExport(req));
    }

    @Operation(summary = "데이터셋별 리포트 출력 이력 조회", description = "지정한 데이터셋에 대해 기록된 리포트 출력 이력 목록을 최신순으로 조회한다.")
    @GetMapping("/api/report-exports/dataset/{datasetId}")
    public ResponseEntity<List<RptExpLogRsp>> findReportsByDataset(
            @Parameter(description = "데이터셋 ID", example = "DS_00000001")
            @PathVariable String datasetId) {
        return ResponseEntity.ok(notiRptService.findReportsByDataset(datasetId));
    }
}
