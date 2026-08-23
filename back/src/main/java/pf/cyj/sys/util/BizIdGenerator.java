package pf.cyj.sys.util;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * ANALYSIS_DATASET 업무키(DATASET_ID/REQUEST_NO) 채번기.
 * DB 트리거(TRG_ANALYSIS_DATASET_BI)와 동일한 포맷을 애플리케이션에서 직접 생성한다 — JPA 는 INSERT 이전에
 * PK 값을 알아야 영속성 컨텍스트를 관리할 수 있기 때문에, 트리거의 NULL 체크 채번에 의존하지 않는다.
 * (트리거는 MyBatis 등 JPA 를 거치지 않는 경로를 위한 안전장치로 그대로 유지된다.)
 */
@Component
@RequiredArgsConstructor
public class BizIdGenerator {

    private final JdbcTemplate jdbcTemplate;

    /** 예: DS0000000001 */
    public String nextDatasetId() {
        Long seq = jdbcTemplate.queryForObject("SELECT SEQ_DATASET_ID.NEXTVAL FROM DUAL", Long.class);
        return "DS" + String.format("%010d", seq);
    }

    /** 예: REQ0000000001 */
    public String nextRequestNo() {
        Long seq = jdbcTemplate.queryForObject("SELECT SEQ_REQUEST_NO.NEXTVAL FROM DUAL", Long.class);
        return "REQ" + String.format("%010d", seq);
    }
}
