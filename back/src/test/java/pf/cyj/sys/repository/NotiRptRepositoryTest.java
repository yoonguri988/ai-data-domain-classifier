package pf.cyj.sys.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.NotiLog;
import pf.cyj.sys.entity.RptExpLog;
import pf.cyj.sys.entity.type.NotiChnl;
import pf.cyj.sys.entity.type.SendStatCd;

/** 알림/리포트 Repository 테스트 - NotiLog(발송이력), RptExpLog(PDF 출력이력). */
@SpringBootTest
@Transactional
class NotiRptRepositoryTest {

    @Autowired
    AppUsrRepository appUsrRepository;
    @Autowired
    AnlDsetRepository anlDsetRepository;
    @Autowired
    NotiLogRepository notiLogRepository;
    @Autowired
    RptExpLogRepository rptExpLogRepository;

    private AppUsr targetUser;
    private AnlDset savedDset;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();
        String sfx = String.valueOf(System.nanoTime() % 100_000);

        targetUser = appUsrRepository.save(
                AppUsr.builder()
                        .loginId("noti-usr-" + ts)
                        .userName("알림수신자")
                        .email("noti-usr-" + ts + "@example.com")
                        .build()
        );

        savedDset = anlDsetRepository.save(
                AnlDset.builder()
                        .datasetId("DS_" + sfx)
                        .requestNo("RQ_" + sfx)
                        .datasetName("알림테스트 데이터셋")
                        .dbSchemaName("TEST_SCHEMA")
                        .tableName("TEST_TABLE")
                        .requestedBy(targetUser)
                        .build()
        );
    }

    @Test
    @DisplayName("NOTIFICATION_LOG 발송 이력 등록 및 수신자별 최신순 조회")
    void testNotiLogFindByTargetUser() {
        NotiLog noti = notiLogRepository.save(
                NotiLog.builder()
                        .channel(NotiChnl.EMAIL)
                        .targetUser(targetUser)
                        .title("표준 도메인 확정 승인 요청")
                        .content("확인이 필요한 승인 요청이 있습니다.")
                        .build()
        );

        assertThat(noti.getSendStatus()).isEqualTo(SendStatCd.SUCCESS);

        List<NotiLog> logs = notiLogRepository.findByTargetUser_UserIdOrderBySentAtDesc(targetUser.getUserId());
        assertThat(logs).extracting(NotiLog::getNotificationId).contains(noti.getNotificationId());
    }

    @Test
    @DisplayName("REPORT_EXPORT_LOG PDF 리포트 출력이력 등록 및 데이터셋별 최신순 조회")
    void testRptExpLogFindByDataset() {
        RptExpLog exp = rptExpLogRepository.save(
                RptExpLog.builder()
                        .anlDset(savedDset)
                        .exportedBy(targetUser)
                        .fileName("domain-prediction-report.pdf")
                        .build()
        );

        List<RptExpLog> logs = rptExpLogRepository.findByAnlDset_DatasetIdOrderByExportedAtDesc(savedDset.getDatasetId());

        assertThat(logs).extracting(RptExpLog::getExportId).contains(exp.getExportId());
        assertThat(logs.get(0).getFileName()).isEqualTo("domain-prediction-report.pdf");
    }
}
