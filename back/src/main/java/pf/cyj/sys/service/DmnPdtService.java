package pf.cyj.sys.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.ai.AiDomainClassifier;
import pf.cyj.sys.ai.AiPredictionCandidate;
import pf.cyj.sys.dto.request.DmnPdtCreateReq;
import pf.cyj.sys.dto.response.DmnCdRsp;
import pf.cyj.sys.dto.response.DmnPdtRsp;
import pf.cyj.sys.entity.AnlCol;
import pf.cyj.sys.entity.DmnCd;
import pf.cyj.sys.entity.DmnPdt;
import pf.cyj.sys.exception.BizRuleException;
import pf.cyj.sys.exception.ExternalApiException;
import pf.cyj.sys.exception.ResourceNotFoundException;
import pf.cyj.sys.repository.AnlColRepository;
import pf.cyj.sys.repository.DmnCdRepository;
import pf.cyj.sys.repository.DmnPdtRepository;

/**
 * 도메인/추천 - 표준 도메인 후보 마스터 조회, AI(외부 API) 판별 결과 저장/조회.
 * predict() 가 실제 외부 AI API 호출(AiDomainClassifier, RestClient 기반) 담당이고,
 * savePrediction() 은 "이미 계산된 판별 결과"를 그대로 저장할 때 쓰는 별도 진입점이다(둘 다 유지).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DmnPdtService {

    /** 동일 컬럼 재판별 캐시 TTL(초) - 1시간. 이 시간 안에는 외부 AI API를 다시 호출하지 않는다. */
    private static final long CACHE_TTL_SECONDS = 3600;
    private static final String CACHE_KEY_PREFIX = "predict:";

    private final AnlColRepository anlColRepository;
    private final DmnCdRepository dmnCdRepository;
    private final DmnPdtRepository dmnPdtRepository;
    private final AiDomainClassifier aiDomainClassifier;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 표준 도메인 후보 마스터 전체를 정렬순서 오름차순으로 조회한다(화면 선택 콤보박스용). */
    public List<DmnCdRsp> findAllDomains() {
        return dmnCdRepository.findAllByOrderBySortOrderAsc().stream().map(DmnCdRsp::from).toList();
    }

    /**
     * 컬럼 1개를 AI로 판별한다(외부 AI API 호출 진입점).
     * 1) Redis 캐시(predict:{columnId})에 결과가 있으면 외부 API를 호출하지 않고 그대로 반환한다
     *    (반환 직전 cacheHitYn 만 true 로 표시 - DB에 저장된 원본 판별결과 자체를 바꾸는 게 아니다).
     * 2) 캐시가 없으면 컬럼 메타를 조회해 AiDomainClassifier 로 외부 AI API를 호출하고,
     *    받아온 Top-N 후보를 DOMAIN_PREDICTION 에 새로 저장한 뒤 그 결과를 캐시에 적재한다.
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public List<DmnPdtRsp> predict(Long columnId) {
        String cacheKey = CACHE_KEY_PREFIX + columnId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List<?> cachedList) {
            return ((List<DmnPdtRsp>) cachedList).stream()
                    .map(rsp -> { rsp.setCacheHitYn(true); return rsp; })
                    .toList();
        }

        AnlCol col = anlColRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 컬럼입니다: " + columnId));
        List<DmnCd> allDomains = dmnCdRepository.findAllByOrderBySortOrderAsc();

        List<AiPredictionCandidate> candidates = aiDomainClassifier.classify(col, allDomains);
        Map<String, DmnCd> domainByCode = allDomains.stream()
                .collect(Collectors.toMap(DmnCd::getDomainCode, d -> d));

        // DOMAIN_PREDICTION은 컬럼당 도메인코드 1건만 허용한다(UQ_DOMAIN_PREDICTION). 이 테이블은 판별
        // 이력이 아니라 "현재 Top-N 스냅샷"이므로, 재판별 시 이전 결과를 지우고 새 결과로 통째로 교체한다.
        // (지우지 않고 새로 INSERT만 하면 같은 컬럼을 두 번째 판별할 때 ORA-00001로 실패한다.)
        dmnPdtRepository.deleteByAnlCol_ColumnId(columnId);
        dmnPdtRepository.flush();

        List<DmnPdtRsp> result = new ArrayList<>();
        int rank = 1;
        for (AiPredictionCandidate candidate : candidates) {
            DmnCd domain = domainByCode.get(candidate.getDomainCode());
            if (domain == null) {
                continue; // AiDomainClassifier 에서 이미 걸러지긴 하지만, 한 번 더 방어적으로 확인한다.
            }
            DmnPdt pdt = DmnPdt.builder()
                    .anlCol(col)
                    .dmnCd(domain)
                    .predictionRank(rank++)
                    .probability(candidate.getProbability())
                    .aiModelName(candidate.getAiModelName())
                    .aiModelVersion(candidate.getAiModelVersion())
                    .responseMs(candidate.getResponseMs())
                    .cacheHitYn(false) // 지금 막 새로 계산해서 저장하는 것이므로 항상 false
                    .build();
            result.add(DmnPdtRsp.from(dmnPdtRepository.save(pdt)));
        }

        if (result.isEmpty()) {
            throw new ExternalApiException("AI_INVALID_RESPONSE", "AI가 유효한 표준 도메인 후보를 반환하지 않았습니다.");
        }

        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofSeconds(CACHE_TTL_SECONDS));
        return result;
    }

    /** 이미 계산된 AI 판별 결과 1건을 저장한다(외부 AI API를 이 메서드 안에서 직접 호출하지는 않는다). */
    @Transactional
    public DmnPdtRsp savePrediction(DmnPdtCreateReq req) {
        AnlCol col = anlColRepository.findById(req.getColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 컬럼입니다: " + req.getColumnId()));
        DmnCd domain = dmnCdRepository.findById(req.getDomainCode())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 도메인 코드입니다: " + req.getDomainCode()));

        // UQ_DOMAIN_PREDICTION(COLUMN_ID, DOMAIN_CODE) - 이미 같은 컬럼+도메인코드 조합이 저장돼 있으면
        // INSERT가 ORA-00001로 실패한다. DB 예외를 그대로 500으로 흘려보내지 않고 409로 미리 안내한다.
        if (dmnPdtRepository.existsByAnlCol_ColumnIdAndDmnCd_DomainCode(req.getColumnId(), req.getDomainCode())) {
            throw new BizRuleException("DUPLICATE_PREDICTION",
                    "이미 저장된 판별 결과입니다(컬럼 " + req.getColumnId() + " / 도메인 " + req.getDomainCode()
                            + "). 같은 컬럼을 다시 판별하려면 POST /api/domain-predictions/columns/{columnId}/predict 를 사용하세요.");
        }

        DmnPdt pdt = DmnPdt.builder()
                .anlCol(col)
                .dmnCd(domain)
                .predictionRank(req.getPredictionRank())
                .probability(req.getProbability())
                .aiModelName(req.getAiModelName())
                .aiModelVersion(req.getAiModelVersion())
                .responseMs(req.getResponseMs())
                .cacheHitYn(Boolean.TRUE.equals(req.getCacheHitYn()))
                .build();

        return DmnPdtRsp.from(dmnPdtRepository.save(pdt));
    }

    /** 컬럼별 AI 판별 결과를 추천순위(predictionRank) 오름차순으로 조회한다(Top-N). */
    public List<DmnPdtRsp> findPredictionsByColumn(Long columnId) {
        return dmnPdtRepository.findByAnlCol_ColumnIdOrderByPredictionRankAsc(columnId)
                .stream().map(DmnPdtRsp::from).toList();
    }
}
