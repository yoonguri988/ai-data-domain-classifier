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
import pf.cyj.sys.entity.CmnCd;
import pf.cyj.sys.entity.CmnCdGrp;

/** 공통코드 그룹 Repository 테스트 - CmnCdGrp, CmnCd(복합키). */
@SpringBootTest
@Transactional
class CmnCdRepositoryTest {

    @Autowired
    CmnCdGrpRepository cmnCdGrpRepository;
    @Autowired
    CmnCdRepository cmnCdRepository;

    private CmnCdGrp savedGrp;

    @BeforeEach
    void setUp() {
        String groupCode = "TEST_GRP_" + System.currentTimeMillis();
        savedGrp = cmnCdGrpRepository.save(
                CmnCdGrp.builder()
                        .codeGroup(groupCode)
                        .groupName("테스트 코드 그룹")
                        .build()
        );
    }

    @Test
    @DisplayName("COMMON_CODE_GROUP 저장 후 codeGroup(PK)로 단건 조회")
    void testFindById() {
        Optional<CmnCdGrp> found = cmnCdGrpRepository.findById(savedGrp.getCodeGroup());

        assertThat(found).isPresent();
        assertThat(found.get().getGroupName()).isEqualTo("테스트 코드 그룹");
    }

    @Test
    @DisplayName("COMMON_CODE 등록 및 그룹별 정렬순서 조회 (복합키)")
    void testCmnCdFindByCodeGroupOrdered() {
        cmnCdRepository.save(
                CmnCd.builder().codeGroup(savedGrp.getCodeGroup()).codeValue("A").codeName("A값").sortOrder(2).build()
        );
        cmnCdRepository.save(
                CmnCd.builder().codeGroup(savedGrp.getCodeGroup()).codeValue("B").codeName("B값").sortOrder(1).build()
        );

        List<CmnCd> ordered = cmnCdRepository.findByCodeGroupOrderBySortOrderAsc(savedGrp.getCodeGroup());

        assertThat(ordered).hasSize(2);
        assertThat(ordered.get(0).getCodeValue()).isEqualTo("B");
        assertThat(ordered.get(1).getCodeValue()).isEqualTo("A");
    }

    @Test
    @DisplayName("사용 여부(useYn=true)로 필터링된 코드만 조회")
    void testCmnCdFindByUseYnTrue() {
        cmnCdRepository.save(
                CmnCd.builder().codeGroup(savedGrp.getCodeGroup()).codeValue("USE").codeName("사용코드").useYn(true).build()
        );
        cmnCdRepository.save(
                CmnCd.builder().codeGroup(savedGrp.getCodeGroup()).codeValue("UNUSE").codeName("미사용코드").useYn(false).build()
        );

        List<CmnCd> useOnly = cmnCdRepository.findByCodeGroupAndUseYnTrueOrderBySortOrderAsc(savedGrp.getCodeGroup());

        assertThat(useOnly).extracting(CmnCd::getCodeValue).containsExactly("USE");
    }
}
