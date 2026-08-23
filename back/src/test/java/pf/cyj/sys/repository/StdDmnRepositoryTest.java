package pf.cyj.sys.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.entity.AnlCol;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.DmnCd;
import pf.cyj.sys.entity.StdDmn;
import pf.cyj.sys.entity.StdDmnReq;
import pf.cyj.sys.entity.type.ReqStatCd;

/** 표준도메인승인 Repository 테스트 - StdDmnReq(신청본), StdDmn(확정본, AnlCol과 PK 공유). */
@SpringBootTest
@Transactional
class StdDmnRepositoryTest {

    @Autowired
    AppUsrRepository appUsrRepository;
    @Autowired
    AnlDsetRepository anlDsetRepository;
    @Autowired
    AnlColRepository anlColRepository;
    @Autowired
    DmnCdRepository dmnCdRepository;
    @Autowired
    StdDmnReqRepository stdDmnReqRepository;
    @Autowired
    StdDmnRepository stdDmnRepository;

    private AppUsr requester;
    private AnlCol savedCol;
    private DmnCd domain;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();
        String sfx = String.valueOf(System.nanoTime() % 100_000);

        requester = appUsrRepository.save(
                AppUsr.builder()
                        .loginId("std-usr-" + ts)
                        .userName("표준도메인담당자")
                        .email("std-usr-" + ts + "@example.com")
                        .build()
        );

        AnlDset dset = anlDsetRepository.save(
                AnlDset.builder()
                        .datasetId("DS_" + sfx)
                        .requestNo("RQ_" + sfx)
                        .datasetName("표준도메인테스트 데이터셋")
                        .dbSchemaName("TEST_SCHEMA")
                        .tableName("TEST_TABLE")
                        .requestedBy(requester)
                        .build()
        );

        savedCol = anlColRepository.save(
                AnlCol.builder().anlDset(dset).columnName("EMAIL_ADDR").build()
        );

        domain = dmnCdRepository.save(
                DmnCd.builder().domainCode("DOM_" + sfx).domainNameKo("연락처").build()
        );
    }

    @Test
    @DisplayName("STANDARD_DOMAIN_REQUEST 신청 등록 및 상태/신청자별 조회")
    void testStdDmnReqFindByStatusAndRequester() {
        StdDmnReq req = stdDmnReqRepository.save(
                StdDmnReq.builder()
                        .anlCol(savedCol)
                        .proposedDmnCd(domain)
                        .aiSuggestedYn(true)
                        .requestedBy(requester)
                        .build()
        );

        assertThat(req.getRequestStatus()).isEqualTo(ReqStatCd.PENDING);

        List<StdDmnReq> pending = stdDmnReqRepository.findByRequestStatusOrderByRequestedAtAsc(ReqStatCd.PENDING);
        assertThat(pending).extracting(StdDmnReq::getRequestId).contains(req.getRequestId());

        List<StdDmnReq> byRequester = stdDmnReqRepository.findByRequestedBy_UserId(requester.getUserId());
        assertThat(byRequester).extracting(StdDmnReq::getRequestId).contains(req.getRequestId());
    }

    @Test
    @DisplayName("STANDARD_DOMAIN 확정본 저장(컬럼과 PK 공유) 및 컬럼 ID로 조회")
    void testStdDmnFindByColumnId() {
        StdDmn confirmed = stdDmnRepository.save(
                StdDmn.builder()
                        .anlCol(savedCol)
                        .dmnCd(domain)
                        .confirmedBy(requester)
                        .build()
        );

        assertThat(confirmed.getColumnId()).isEqualTo(savedCol.getColumnId());

        Optional<StdDmn> found = stdDmnRepository.findByAnlCol_ColumnId(savedCol.getColumnId());
        assertThat(found).isPresent();
        assertThat(found.get().getVersionNo()).isEqualTo(1);
        assertThat(found.get().getDmnCd().getDomainCode()).isEqualTo(domain.getDomainCode());
    }
}
