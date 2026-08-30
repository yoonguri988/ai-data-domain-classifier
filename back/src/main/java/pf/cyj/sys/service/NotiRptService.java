package pf.cyj.sys.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.dto.request.NotiLogCreateReq;
import pf.cyj.sys.dto.request.RptExpLogCreateReq;
import pf.cyj.sys.dto.response.NotiLogRsp;
import pf.cyj.sys.dto.response.RptExpLogRsp;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.NotiLog;
import pf.cyj.sys.entity.RptExpLog;
import pf.cyj.sys.entity.StdDmnReq;
import pf.cyj.sys.entity.type.NotiChnl;
import pf.cyj.sys.entity.type.SendStatCd;
import pf.cyj.sys.exception.ResourceNotFoundException;
import pf.cyj.sys.notification.NotificationSender;
import pf.cyj.sys.repository.AnlDsetRepository;
import pf.cyj.sys.repository.AppUsrRepository;
import pf.cyj.sys.repository.NotiLogRepository;
import pf.cyj.sys.repository.RptExpLogRepository;
import pf.cyj.sys.repository.StdDmnReqRepository;

/**
 * 알림/리포트 - 알림은 실제 발송(NotificationSender: 이메일)까지 하고 그 결과를 이력으로 남긴다.
 * 리포트 출력 이력도 여기서 기록한다 - 실제 PDF 생성(PdfReportService, v4.8)은
 * AnlDsetColController.downloadReport() 가 recordReportExport() 를 직접 호출해서 다운로드와 동시에
 * 이력을 남기고, 이 클래스의 recordReportExport(RptExpLogCreateReq) 는 그 내부에서 재사용되는
 * 동시에 REST(POST /api/report-exports)로 수동 기록할 때도 쓰인다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotiRptService {

    private final AppUsrRepository appUsrRepository;
    private final AnlDsetRepository anlDsetRepository;
    private final StdDmnReqRepository stdDmnReqRepository;
    private final NotiLogRepository notiLogRepository;
    private final RptExpLogRepository rptExpLogRepository;
    private final NotificationSender notificationSender;

    /** 알림 발송 요청(REST) - 대상 사용자/연관 신청건을 ID로 조회한 뒤 실제 발송까지 처리한다. */
    @Transactional
    public NotiLogRsp recordNotification(NotiLogCreateReq req) {
        AppUsr target = appUsrRepository.findById(req.getTargetUserId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 수신 대상입니다: " + req.getTargetUserId()));

        StdDmnReq relatedReq = null;
        if (req.getRelatedRequestId() != null) {
            relatedReq = stdDmnReqRepository.findById(req.getRelatedRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 신청 건입니다: " + req.getRelatedRequestId()));
        }

        return sendAndRecord(target, relatedReq, req.getTitle(), req.getContent());
    }

    /**
     * 알림을 실제로 발송(NotificationSender, 이메일)하고 그 결과를 이력으로 남긴다.
     * StdDmnService.review() 처럼 승인/반려 같은 시스템 이벤트가 엔티티를 이미 들고 있는 상태에서
     * 곧바로 호출하는 내부용 API다(REST 요청 DTO를 거치지 않고 엔티티를 직접 받는다).
     * 발송이 실패해도 예외를 던지지 않고 SEND_STATUS=FAIL 로 이력을 남긴다 - 알림 발송 실패가
     * 승인/반려 같은 원래 하려던 작업 자체를 실패시켜서는 안 되기 때문이다.
     */
    @Transactional
    public NotiLogRsp sendAndRecord(AppUsr target, StdDmnReq relatedReq, String title, String content) {
        boolean sent = notificationSender.sendEmail(target.getEmail(), title, content);

        NotiLog noti = NotiLog.builder()
                .channel(NotiChnl.EMAIL)
                .targetUser(target)
                .stdDmnReq(relatedReq)
                .title(title)
                .content(content)
                .sendStatus(sent ? SendStatCd.SUCCESS : SendStatCd.FAIL)
                .build();

        return NotiLogRsp.from(notiLogRepository.save(noti));
    }

    /** 특정 사용자에게 발송된 알림 이력을 최신순으로 조회한다. */
    public List<NotiLogRsp> findNotificationsByUser(Long userId) {
        return notiLogRepository.findByTargetUser_UserIdOrderBySentAtDesc(userId).stream().map(NotiLogRsp::from).toList();
    }

    /** 리포트 출력 이력 1건을 기록한다(실제 PDF 생성 연동은 아직 붙어있지 않고, 이력만 남긴다). */
    @Transactional
    public RptExpLogRsp recordReportExport(RptExpLogCreateReq req) {
        AnlDset dset = anlDsetRepository.findById(req.getDatasetId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 데이터셋입니다: " + req.getDatasetId()));
        AppUsr exporter = appUsrRepository.findById(req.getExportedById())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다: " + req.getExportedById()));

        RptExpLog exp = RptExpLog.builder()
                .anlDset(dset)
                .exportedBy(exporter)
                .fileName(req.getFileName())
                .build();

        return RptExpLogRsp.from(rptExpLogRepository.save(exp));
    }

    /** 특정 데이터셋에 대해 기록된 리포트 출력 이력을 최신순으로 조회한다. */
    public List<RptExpLogRsp> findReportsByDataset(String datasetId) {
        return rptExpLogRepository.findByAnlDset_DatasetIdOrderByExportedAtDesc(datasetId)
                .stream().map(RptExpLogRsp::from).toList();
    }
}
