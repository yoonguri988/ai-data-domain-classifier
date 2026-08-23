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
import pf.cyj.sys.entity.type.DsetStatCd;

/** 분석대상 Repository 테스트 - AnlDset(업무키 PK), AnlCol. */
@SpringBootTest
@Transactional
class AnlDsetColRepositoryTest {

    @Autowired
    AppUsrRepository appUsrRepository;
    @Autowired
    AnlDsetRepository anlDsetRepository;
    @Autowired
    AnlColRepository anlColRepository;

    private AppUsr requester;
    private AnlDset savedDset;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();
        String sfx = String.valueOf(System.nanoTime() % 100_000);

        requester = appUsrRepository.save(
                AppUsr.builder()
                        .loginId("dset-usr-" + ts)
                        .userName("데이터셋담당자")
                        .email("dset-usr-" + ts + "@example.com")
                        .build()
        );

        // DATASET_ID/REQUEST_NO 는 실제로는 서비스단 BizIdGenerator 가 채번하지만,
        // Repository 단위 테스트에서는 업무키를 직접 세팅한다 (컬럼 길이 20자 제약 준수).
        savedDset = anlDsetRepository.save(
                AnlDset.builder()
                        .datasetId("DS_" + sfx)
                        .requestNo("RQ_" + sfx)
                        .datasetName("테스트 데이터셋")
                        .dbSchemaName("TEST_SCHEMA")
                        .tableName("TEST_TABLE")
                        .requestedBy(requester)
                        .build()
        );
    }

    @Test
    @DisplayName("ANALYSIS_DATASET 저장 후 업무키(datasetId)로 단건 조회")
    void testFindById() {
        Optional<AnlDset> found = anlDsetRepository.findById(savedDset.getDatasetId());

        assertThat(found).isPresent();
        assertThat(found.get().getDatasetStatus()).isEqualTo(DsetStatCd.REGISTERED);
        assertThat(found.get().getRequestedBy().getUserId()).isEqualTo(requester.getUserId());
    }

    @Test
    @DisplayName("requestNo 중복 여부 확인 및 상태별/등록자별 조회")
    void testExistsAndFilters() {
        assertThat(anlDsetRepository.existsByRequestNo(savedDset.getRequestNo())).isTrue();

        List<AnlDset> byStatus = anlDsetRepository.findByDatasetStatus(DsetStatCd.REGISTERED);
        assertThat(byStatus).extracting(AnlDset::getDatasetId).contains(savedDset.getDatasetId());

        List<AnlDset> byRequester = anlDsetRepository.findByRequestedBy_UserId(requester.getUserId());
        assertThat(byRequester).extracting(AnlDset::getDatasetId).contains(savedDset.getDatasetId());
    }

    @Test
    @DisplayName("ANALYSIS_COLUMN 등록 및 데이터셋 기준 컬럼 목록 조회")
    void testAnlColFindByDataset() {
        anlColRepository.save(
                AnlCol.builder()
                        .anlDset(savedDset)
                        .columnName("USER_ID")
                        .dataType("VARCHAR2")
                        .numericYn(false)
                        .uniqueYn(true)
                        .build()
        );
        anlColRepository.save(
                AnlCol.builder()
                        .anlDset(savedDset)
                        .columnName("AMOUNT")
                        .dataType("NUMBER")
                        .numericYn(true)
                        .build()
        );

        List<AnlCol> columns = anlColRepository.findByAnlDset_DatasetId(savedDset.getDatasetId());

        assertThat(columns).hasSize(2);
        assertThat(columns).extracting(AnlCol::getColumnName).containsExactlyInAnyOrder("USER_ID", "AMOUNT");
    }
}
