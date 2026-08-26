package pf.cyj.sys.ai;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AiDomainClassifier 가 외부 AI API 호출 1건에서 뽑아낸 표준 도메인 후보 1개.
 * DmnPdtService 가 이 값을 그대로 DOMAIN_PREDICTION 엔티티(DmnPdt)에 옮겨 저장한다.
 * aiModelName/aiModelVersion/responseMs 는 호출 1건에 대해 모든 후보가 같은 값을 공유한다
 * (API 요청 자체가 컬럼 1개당 1번만 나가고, 그 응답 안에 Top-N 후보가 함께 들어있기 때문).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiPredictionCandidate {

    /** DOMAIN_CODE 마스터에 존재하는 코드여야 한다(그렇지 않으면 DmnPdtService 가 방어적으로 걸러낸다) */
    private String domainCode;

    /** 0.00000 ~ 1.00000 */
    private BigDecimal probability;

    private String aiModelName;

    private String aiModelVersion;

    private Integer responseMs;
}
