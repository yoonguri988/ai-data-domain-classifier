package pf.cyj.sys.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
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
import pf.cyj.sys.entity.DmnPdt;

/** 도메인/추천 Repository 테스트 - DmnCd, DmnPdt(AI 판별 결과 Top-N). */
@SpringBootTest
@Transactional
class DmnPdtRepositoryTest {

    @Autowired
    AppUsrRepository appUsrRepository;
    @Autowired
    AnlDsetRepository anlDsetRepository;
    @Autowired
    AnlColRepository anlColRepository;
    @Autowired
    DmnCdRepository dmnCdRepository;
    @Autowired
    DmnPdtRepository dmnPdtRepository;

    private AnlCol savedCol;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();
        String sfx = String.valueOf(System.nanoTime() % 100_000);

        AppUsr usr = appUsrRepository.save(
                AppUsr.builder()
                        .loginId("dmn-usr-" + ts)
                        .userName("도메인담당자")
                        .email("dmn-usr-" + ts + "@example.com")
                        .build()
        );

        AnlDset dset = anlDsetRepository.save(
                AnlDset.builder()
                        .datasetId("DS_" + sfx)
                        .requestNo("RQ_" + sfx)
                        .datasetName("도메인테스트 데이터셋")
                        .dbSchemaName("TEST_SCHEMA")
                        .tableName("TEST_TABLE")
                        .requestedBy(usr)
                        .build()
        );

        savedCol = anlColRepository.save(
                AnlCol.builder().anlDset(dset).columnName("PHONE_NO").build()
        );
    }

    @Test
    @DisplayName("DOMAIN_CODE 등록 및 정렬순서 오름차순 조회")
    void testDmnCdFindAllOrdered() {
        dmnCdRepository.save(
                DmnCd.builder()
                        .domainCode("D2_" + (System.nanoTime() % 100_000))
                        .domainNameKo("연락처")
                        .sortOrder(2)
                        .build()
        );
        dmnCdRepository.save(
                DmnCd.builder()
                        .domainCode("D1_" + (System.nanoTime() % 100_000))
                        .domainNameKo("아이디")
                        .sortOrder(1)
                        .build()
        );

        List<DmnCd> ordered = dmnCdRepository.findAllByOrderBySortOrderAsc();

        assertThat(ordered).isNotEmpty();
        // 기존 시드 데이터와 섞여도 무관하도록 전체가 정렬순서 오름차순인지만 확인
        for (int i = 1; i < ordered.size(); i++) {
            assertThat(ordered.get(i).getSortOrder()).isGreaterThanOrEqualTo(ordered.get(i - 1).getSortOrder());
        }
    }

    @Test
    @DisplayName("DOMAIN_PREDICTION 등록 및 컬럼별 추천순위(Top-N) 조회")
    void testDmnPdtFindByColumnOrderedByRank() {
    	DmnCd domain1 = dmnCdRepository.save(
                DmnCd.builder()
                        .domainCode("DOM1_" + (System.nanoTime() % 100_000))
                        .domainNameKo("연락처")
                        .build()
        );

        DmnCd domain2 = dmnCdRepository.save(
                DmnCd.builder()
                        .domainCode("DOM2_" + (System.nanoTime() % 100_000))
                        .domainNameKo("이메일")
                        .build()
        );

        DmnPdt rank2 = dmnPdtRepository.save(
                DmnPdt.builder()
                        .anlCol(savedCol)
                        .dmnCd(domain2)
                        .predictionRank(2)
                        .probability(new BigDecimal("0.31000"))
                        .aiModelName("claude-haiku")
                        .build()
        );
        DmnPdt rank1 = dmnPdtRepository.save(
                DmnPdt.builder()
                        .anlCol(savedCol)
                        .dmnCd(domain1)
                        .predictionRank(1)
                        .probability(new BigDecimal("0.87000"))
                        .aiModelName("claude-haiku")
                        .build()
        );

        List<DmnPdt> predictions = dmnPdtRepository.findByAnlCol_ColumnIdOrderByPredictionRankAsc(savedCol.getColumnId());

        assertThat(predictions).hasSize(2);
        assertThat(predictions.get(0).getPredictionId()).isEqualTo(rank1.getPredictionId());
        assertThat(predictions.get(1).getPredictionId()).isEqualTo(rank2.getPredictionId());
        assertThat(predictions.get(0).isCacheHitYn()).isFalse();
    }
}
