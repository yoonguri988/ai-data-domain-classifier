package pf.cyj.sys.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.dto.request.DmnPdtCreateReq;
import pf.cyj.sys.dto.response.DmnCdRsp;
import pf.cyj.sys.dto.response.DmnPdtRsp;
import pf.cyj.sys.entity.AnlCol;
import pf.cyj.sys.entity.DmnCd;
import pf.cyj.sys.entity.DmnPdt;
import pf.cyj.sys.exception.ResourceNotFoundException;
import pf.cyj.sys.repository.AnlColRepository;
import pf.cyj.sys.repository.DmnCdRepository;
import pf.cyj.sys.repository.DmnPdtRepository;

/**
 * 도메인/추천 - 표준 도메인 후보 마스터 조회, AI(외부 API) 판별 결과 저장/조회.
 * 외부 AI API 호출(RestClient 기반 AiDomainClassifier)은 ai.* 설정이 활성화되는 다음 단계(외부 연동)에서 붙인다.
 * 이 Service 는 "이미 계산된 판별 결과"를 저장하고 컬럼별 Top-N 을 조회하는 역할까지만 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DmnPdtService {

    private final AnlColRepository anlColRepository;
    private final DmnCdRepository dmnCdRepository;
    private final DmnPdtRepository dmnPdtRepository;

    /** 표준 도메인 후보 마스터 전체를 정렬순서 오름차순으로 조회한다(화면 선택 콤보박스용). */
    public List<DmnCdRsp> findAllDomains() {
        return dmnCdRepository.findAllByOrderBySortOrderAsc().stream().map(DmnCdRsp::from).toList();
    }

    /** 이미 계산된 AI 판별 결과 1건을 저장한다(외부 AI API 호출 자체는 아직 이 Service 에 붙어있지 않다). */
    @Transactional
    public DmnPdtRsp savePrediction(DmnPdtCreateReq req) {
        AnlCol col = anlColRepository.findById(req.getColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 컬럼입니다: " + req.getColumnId()));
        DmnCd domain = dmnCdRepository.findById(req.getDomainCode())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 도메인 코드입니다: " + req.getDomainCode()));

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
