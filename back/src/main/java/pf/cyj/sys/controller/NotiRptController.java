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
import pf.cyj.sys.dto.request.NotiLogCreateReq;
import pf.cyj.sys.dto.request.RptExpLogCreateReq;
import pf.cyj.sys.dto.response.NotiLogRsp;
import pf.cyj.sys.dto.response.RptExpLogRsp;
import pf.cyj.sys.oauth2.CustomUserPrincipal;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.service.NotiRptService;

/**
 * 알림/리포트 - 발송/출력 이력 기록 및 조회. 전부 로그인이 필요하다.
 * 조회는 본인 이력만 노출한다(findNotificationsByUser 를 ActorContext.userId() 로만 호출 —
 * 다른 사용자 ID 를 파라미터로 받지 않아 IDOR 을 원천 차단). 실제 발송(coolsms/mail)과
 * PDF 생성(PDFBox)은 NotiRptService 문서와 동일하게 외부 연동 단계에서 붙는다.
 */
@RestController
@RequiredArgsConstructor
public class NotiRptController {

    private final NotiRptService notiRptService;

    @PostMapping("/api/notifications")
    public ResponseEntity<NotiLogRsp> recordNotification(@Valid @RequestBody NotiLogCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notiRptService.recordNotification(req));
    }

    @GetMapping("/api/notifications/mine")
    public ResponseEntity<List<NotiLogRsp>> findMyNotifications(@AuthenticationPrincipal CustomUserPrincipal principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.ok(notiRptService.findNotificationsByUser(actor.userId()));
    }

    @PostMapping("/api/report-exports")
    public ResponseEntity<RptExpLogRsp> recordReportExport(@Valid @RequestBody RptExpLogCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notiRptService.recordReportExport(req));
    }

    @GetMapping("/api/report-exports/dataset/{datasetId}")
    public ResponseEntity<List<RptExpLogRsp>> findReportsByDataset(@PathVariable String datasetId) {
        return ResponseEntity.ok(notiRptService.findReportsByDataset(datasetId));
    }
}
