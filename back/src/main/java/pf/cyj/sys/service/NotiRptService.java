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
import pf.cyj.sys.exception.ResourceNotFoundException;
import pf.cyj.sys.repository.AnlDsetRepository;
import pf.cyj.sys.repository.AppUsrRepository;
import pf.cyj.sys.repository.NotiLogRepository;
import pf.cyj.sys.repository.RptExpLogRepository;
import pf.cyj.sys.repository.StdDmnReqRepository;

/**
 * 알림/리포트 - 발송이력/출력이력 기록 및 조회.
 * 실제 발송(coolsms/spring-boot-starter-mail)과 PDF 생성(PDFBox)은 build.gradle 에서 아직 주석 처리된
 * 외부 연동이 붙는 다음 단계에서 구현하며, 이 Service 는 그 결과를 기록·조회하는 역할까지만 담당한다.
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

    @Transactional
    public NotiLogRsp recordNotification(NotiLogCreateReq req) {
        AppUsr target = appUsrRepository.findById(req.getTargetUserId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 수신 대상입니다: " + req.getTargetUserId()));

        StdDmnReq relatedReq = null;
        if (req.getRelatedRequestId() != null) {
            relatedReq = stdDmnReqRepository.findById(req.getRelatedRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 신청 건입니다: " + req.getRelatedRequestId()));
        }

        NotiLog noti = NotiLog.builder()
                .channel(NotiChnl.valueOf(req.getChannel()))
                .targetUser(target)
                .stdDmnReq(relatedReq)
                .title(req.getTitle())
                .content(req.getContent())
                .build();

        return NotiLogRsp.from(notiLogRepository.save(noti));
    }

    public List<NotiLogRsp> findNotificationsByUser(Long userId) {
        return notiLogRepository.findByTargetUser_UserIdOrderBySentAtDesc(userId).stream().map(NotiLogRsp::from).toList();
    }

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

    public List<RptExpLogRsp> findReportsByDataset(String datasetId) {
        return rptExpLogRepository.findByAnlDset_DatasetIdOrderByExportedAtDesc(datasetId)
                .stream().map(RptExpLogRsp::from).toList();
    }
}
