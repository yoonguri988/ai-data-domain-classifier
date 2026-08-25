package pf.cyj.sys.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import pf.cyj.sys.dto.request.AnlColBulkCreateReq;
import pf.cyj.sys.dto.request.AnlDsetCreateReq;
import pf.cyj.sys.dto.response.AnlColRsp;
import pf.cyj.sys.dto.response.AnlDsetRsp;
import pf.cyj.sys.oauth2.CustomOAuth2User;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.service.AnlDsetColService;

/**
 * 분석대상 - 데이터셋 등록/조회, 컬럼 메타 일괄 등록/조회. 전부 로그인이 필요하다
 * (SecurityConfig 의 anyRequest().authenticated() 기본값을 그대로 따른다).
 * "/mine" 은 "/{datasetId}" 보다 먼저 선언하지 않아도 Spring 이 리터럴 경로를 변수 경로보다
 * 우선 매칭하므로 순서와 무관하게 정상 동작한다.
 */
@Tag(name = "AnalysisTarget", description = "분석대상 - 데이터셋/컬럼 메타 등록 및 조회 (로그인 필요)")
@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class AnlDsetColController {

    private final AnlDsetColService anlDsetColService;

    @Operation(summary = "데이터셋 등록", description = "분석 대상 데이터셋을 신규 등록한다. 등록자는 인증 정보에서 자동으로 채워진다.")
    @PostMapping
    public ResponseEntity<AnlDsetRsp> createDset(
            @Valid @RequestBody AnlDsetCreateReq req,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(anlDsetColService.createDset(req, actor.getUserId()));
    }

    @Operation(summary = "내 데이터셋 목록 조회", description = "현재 로그인한 사용자가 요청한 데이터셋 목록을 조회한다.")
    @GetMapping("/mine")
    public ResponseEntity<List<AnlDsetRsp>> findMyDsets(@AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.ok(anlDsetColService.findDsetByRequester(actor.getUserId()));
    }

    @Operation(summary = "데이터셋 단건 조회", description = "데이터셋 ID로 단건 상세를 조회한다.")
    @GetMapping("/{datasetId}")
    public ResponseEntity<AnlDsetRsp> findDsetById(
            @Parameter(description = "데이터셋 ID") @PathVariable String datasetId) {
        return ResponseEntity.ok(anlDsetColService.findDsetById(datasetId));
    }

    @Operation(summary = "컬럼 메타 일괄 등록", description = "하나의 데이터셋에 속한 컬럼 메타 정보를 여러 건 한 번에 등록한다.")
    @PostMapping("/columns")
    public ResponseEntity<List<AnlColRsp>> createColumnsBulk(@Valid @RequestBody AnlColBulkCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anlDsetColService.createColumnsBulk(req));
    }

    @Operation(summary = "데이터셋별 컬럼 목록 조회", description = "지정한 데이터셋에 등록된 컬럼 메타 목록을 조회한다.")
    @GetMapping("/{datasetId}/columns")
    public ResponseEntity<List<AnlColRsp>> findColumnsByDataset(
            @Parameter(description = "데이터셋 ID") @PathVariable String datasetId) {
        return ResponseEntity.ok(anlDsetColService.findColumnsByDataset(datasetId));
    }
}
