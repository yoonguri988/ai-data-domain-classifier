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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pf.cyj.sys.dto.request.AnlJobCreateReq;
import pf.cyj.sys.dto.response.AnlJobLogRsp;
import pf.cyj.sys.dto.response.AnlJobRsp;
import pf.cyj.sys.entity.type.JobStatCd;
import pf.cyj.sys.oauth2.CustomUserPrincipal;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.service.AnlJobService;

/**
 * 배치작업 - 분석/재판별 배치 등록·조회, 실행 이력 조회. 전부 로그인이 필요하다.
 * startExecution/completeExecution/failExecution 은 AnlJobService 문서에도 명시했듯
 * 배치 실행기(AbstractAnlJobExecutor, 추후 구현)가 내부적으로 호출하는 용도라 REST 로 노출하지 않는다.
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class AnlJobController {

    private final AnlJobService anlJobService;

    @PostMapping
    public ResponseEntity<AnlJobRsp> createJob(
            @Valid @RequestBody AnlJobCreateReq req,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(anlJobService.createJob(req, actor.userId()));
    }

    @GetMapping
    public ResponseEntity<List<AnlJobRsp>> findJobsByStatus(@RequestParam JobStatCd status) {
        return ResponseEntity.ok(anlJobService.findJobsByStatus(status));
    }

    @GetMapping("/dataset/{datasetId}")
    public ResponseEntity<List<AnlJobRsp>> findJobsByDataset(@PathVariable String datasetId) {
        return ResponseEntity.ok(anlJobService.findJobsByDataset(datasetId));
    }

    @GetMapping("/{jobId}/logs")
    public ResponseEntity<List<AnlJobLogRsp>> findLogsByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(anlJobService.findLogsByJob(jobId));
    }
}
