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
import pf.cyj.sys.dto.request.AnlColBulkCreateReq;
import pf.cyj.sys.dto.request.AnlDsetCreateReq;
import pf.cyj.sys.dto.response.AnlColRsp;
import pf.cyj.sys.dto.response.AnlDsetRsp;
import pf.cyj.sys.oauth2.CustomUserPrincipal;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.service.AnlDsetColService;

/**
 * 분석대상 - 데이터셋 등록/조회, 컬럼 메타 일괄 등록/조회. 전부 로그인이 필요하다
 * (SecurityConfig 의 anyRequest().authenticated() 기본값을 그대로 따른다).
 * "/mine" 은 "/{datasetId}" 보다 먼저 선언하지 않아도 Spring 이 리터럴 경로를 변수 경로보다
 * 우선 매칭하므로 순서와 무관하게 정상 동작한다.
 */
@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class AnlDsetColController {

    private final AnlDsetColService anlDsetColService;

    @PostMapping
    public ResponseEntity<AnlDsetRsp> createDset(
            @Valid @RequestBody AnlDsetCreateReq req,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(anlDsetColService.createDset(req, actor.userId()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<AnlDsetRsp>> findMyDsets(@AuthenticationPrincipal CustomUserPrincipal principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.ok(anlDsetColService.findDsetByRequester(actor.userId()));
    }

    @GetMapping("/{datasetId}")
    public ResponseEntity<AnlDsetRsp> findDsetById(@PathVariable String datasetId) {
        return ResponseEntity.ok(anlDsetColService.findDsetById(datasetId));
    }

    @PostMapping("/columns")
    public ResponseEntity<List<AnlColRsp>> createColumnsBulk(@Valid @RequestBody AnlColBulkCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anlDsetColService.createColumnsBulk(req));
    }

    @GetMapping("/{datasetId}/columns")
    public ResponseEntity<List<AnlColRsp>> findColumnsByDataset(@PathVariable String datasetId) {
        return ResponseEntity.ok(anlDsetColService.findColumnsByDataset(datasetId));
    }
}
