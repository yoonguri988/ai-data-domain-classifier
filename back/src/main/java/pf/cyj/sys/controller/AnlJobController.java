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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import pf.cyj.sys.dto.request.AnlJobCreateReq;
import pf.cyj.sys.dto.response.AnlJobLogRsp;
import pf.cyj.sys.dto.response.AnlJobRsp;
import pf.cyj.sys.entity.type.JobStatCd;
import pf.cyj.sys.oauth2.CustomOAuth2User;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.service.AnlJobService;

/**
 * 배치작업 - 분석/재판별 배치 등록·조회, 실행 이력 조회. 전부 로그인이 필요하다.
 * startExecution/completeExecution/failExecution 은 AnlJobService 문서에도 명시했듯
 * 배치 실행기(AbstractAnlJobExecutor, 추후 구현)가 내부적으로 호출하는 용도라 REST 로 노출하지 않는다.
 */
@Tag(name = "AnalysisJob", description = "배치작업 - 분석/재판별 배치 등록·조회 및 실행 이력 조회")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class AnlJobController {

    private final AnlJobService anlJobService;

    @Operation(summary = "배치작업 등록", description = "데이터셋에 대한 분석/재판별 배치작업을 신규 등록한다. 등록자는 인증 정보에서 자동으로 채워진다.")
    @PostMapping
    public ResponseEntity<AnlJobRsp> createJob(
            @Valid @RequestBody AnlJobCreateReq req,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(anlJobService.createJob(req, actor.getUserId()));
    }

    @Operation(summary = "상태별 배치작업 목록 조회", description = "지정한 실행 상태(JobStatCd)의 배치작업 목록을 조회한다.")
    @GetMapping
    public ResponseEntity<List<AnlJobRsp>> findJobsByStatus(
            @Parameter(description = "배치작업 실행 상태 (예: WAIT, RUNNING, DONE, FAILED)") @RequestParam JobStatCd status) {
        return ResponseEntity.ok(anlJobService.findJobsByStatus(status));
    }

    @Operation(summary = "데이터셋별 배치작업 목록 조회", description = "지정한 데이터셋에 등록된 배치작업 목록을 조회한다.")
    @GetMapping("/dataset/{datasetId}")
    public ResponseEntity<List<AnlJobRsp>> findJobsByDataset(
            @Parameter(description = "데이터셋 ID") @PathVariable String datasetId) {
        return ResponseEntity.ok(anlJobService.findJobsByDataset(datasetId));
    }

    @Operation(summary = "배치작업 실행 이력 조회", description = "지정한 배치작업의 실행 로그(시작/완료/실패 이력)를 조회한다.")
    @GetMapping("/{jobId}/logs")
    public ResponseEntity<List<AnlJobLogRsp>> findLogsByJob(
            @Parameter(description = "배치작업 ID") @PathVariable Long jobId) {
        return ResponseEntity.ok(anlJobService.findLogsByJob(jobId));
    }
}
