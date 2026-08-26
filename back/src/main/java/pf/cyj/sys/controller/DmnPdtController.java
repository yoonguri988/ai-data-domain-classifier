package pf.cyj.sys.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import pf.cyj.sys.dto.request.DmnPdtCreateReq;
import pf.cyj.sys.dto.response.DmnCdRsp;
import pf.cyj.sys.dto.response.DmnPdtRsp;
import pf.cyj.sys.service.DmnPdtService;

/**
 * 도메인/추천 - 표준 도메인 후보 마스터 조회(GET "/api/domains" 는 SecurityConfig 에서 비로그인 허용,
 * 화면의 도메인 선택 콤보박스용)와 AI 판별 결과 저장/조회("/api/domain-predictions" 는 로그인 필요).
 *
 * <p>Swagger UI 테스트 순서 예시: (1) POST /api/datasets 로 데이터셋 등록 → (2) 등록된 datasetId로
 * 컬럼 메타를 등록(AnlDsetColController) → (3) POST /api/domain-predictions/columns/{columnId}/predict
 * 로 실제 AI 판별 요청(외부 AI API 호출 + Redis 캐시) → (4) GET /api/domain-predictions/columns/{columnId}
 * 로 저장된 결과 확인.
 */
@Tag(name = "DomainPrediction", description = "도메인/추천 - 표준 도메인 후보 조회 및 AI 판별 결과 저장/조회")
@RestController
@RequiredArgsConstructor
public class DmnPdtController {

    private final DmnPdtService dmnPdtService;

    @Operation(summary = "표준 도메인 후보 전체 조회", description = "정렬순서(sortOrder) 오름차순으로 등록된 모든 표준 도메인 후보를 조회한다. 비로그인으로도 호출 가능하다.")
    @GetMapping("/api/domains")
    public ResponseEntity<List<DmnCdRsp>> findAllDomains() {
        return ResponseEntity.ok(dmnPdtService.findAllDomains());
    }

    @Operation(
            summary = "AI 판별 요청 (외부 AI API 연동)",
            description = "지정한 컬럼의 메타데이터를 외부 AI API(OpenAI Chat Completions)에 보내 표준 도메인 후보 "
                    + "Top-N을 판별하고 그 결과를 DOMAIN_PREDICTION에 저장한다. 같은 컬럼을 1시간 이내에 다시 요청하면 "
                    + "외부 API를 다시 호출하지 않고 Redis에 캐시된 이전 결과를 그대로 반환한다(응답의 cacheHitYn=true로 "
                    + "확인 가능). 캐시가 만료된 뒤 다시 판별하면(재판별) 그 컬럼의 이전 Top-N은 지워지고 새 결과로 통째로 "
                    + "교체된다(컬럼당 도메인코드 1건만 저장 가능 - UQ_DOMAIN_PREDICTION). "
                    + "외부 API 호출/응답 파싱에 실패하면 502(errorCode=AI_CALL_ERROR 또는 AI_INVALID_RESPONSE)로 응답한다."
    )
    @PostMapping("/api/domain-predictions/columns/{columnId}/predict")
    public ResponseEntity<List<DmnPdtRsp>> predict(
            @Parameter(description = "판별 대상 분석 컬럼 ID", example = "1")
            @PathVariable(value = "columnId") Long columnId) {
        return ResponseEntity.ok(dmnPdtService.predict(columnId));
    }

    @Operation(
            summary = "AI 판별 결과 저장 (이미 계산된 결과 직접 등록용)",
            description = "이미 계산된 컬럼별 도메인 판별 결과(순위/확률/AI 모델 정보)를 저장한다. "
                    + "실제 외부 AI API 호출은 이 시점에는 수행하지 않는다 - 호출은 이미 완료됐다고 가정하고 그 결과값만 저장한다. "
                    + "보통은 위 predict 엔드포인트를 쓰면 되고, 이 API는 테스트 데이터를 직접 넣고 싶을 때 사용한다. "
                    + "같은 컬럼+도메인코드 조합이 이미 저장돼 있으면 409(errorCode=DUPLICATE_PREDICTION)로 응답한다."
    )
    @PostMapping("/api/domain-predictions")
    public ResponseEntity<DmnPdtRsp> savePrediction(@Valid @RequestBody DmnPdtCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dmnPdtService.savePrediction(req));
    }

    @Operation(summary = "컬럼별 판별 결과(Top-N) 조회", description = "지정한 컬럼에 대해 저장된 AI 판별 결과를 추천순위(predictionRank) 오름차순으로 조회한다.")
    @GetMapping("/api/domain-predictions/columns/{columnId}")
    public ResponseEntity<List<DmnPdtRsp>> findPredictionsByColumn(
            @Parameter(description = "분석 컬럼 ID", example = "1")
            @PathVariable(value = "columnId") Long columnId) {
        return ResponseEntity.ok(dmnPdtService.findPredictionsByColumn(columnId));
    }
}
