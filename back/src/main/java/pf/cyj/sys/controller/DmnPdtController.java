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
 * 실제 외부 AI API 호출은 아직 붙지 않았고(다음 "외부 연동" 단계 예정), 여기서는 이미 계산된
 * 판별 결과를 저장/조회하는 역할까지만 담당한다(DmnPdtService 와 동일한 범위).
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
            summary = "AI 판별 결과 저장",
            description = "이미 계산된 컬럼별 도메인 판별 결과(순위/확률/AI 모델 정보)를 저장한다. "
                    + "실제 외부 AI API 호출은 이 시점에는 수행하지 않는다 - 호출은 이미 완료됐다고 가정하고 그 결과값만 저장한다."
    )
    @PostMapping("/api/domain-predictions")
    public ResponseEntity<DmnPdtRsp> savePrediction(@Valid @RequestBody DmnPdtCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dmnPdtService.savePrediction(req));
    }

    @Operation(summary = "컬럼별 판별 결과(Top-N) 조회", description = "지정한 컬럼에 대해 저장된 AI 판별 결과를 추천순위(predictionRank) 오름차순으로 조회한다.")
    @GetMapping("/api/domain-predictions/columns/{columnId}")
    public ResponseEntity<List<DmnPdtRsp>> findPredictionsByColumn(
            @Parameter(description = "분석 컬럼 ID", example = "1")
            @PathVariable(value="columnId") Long columnId) {
        return ResponseEntity.ok(dmnPdtService.findPredictionsByColumn(columnId));
    }
}
