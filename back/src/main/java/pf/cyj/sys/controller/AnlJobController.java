package pf.cyj.sys.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import pf.cyj.sys.job.DomainPredictJobExecutor;
import pf.cyj.sys.oauth2.CustomOAuth2User;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.service.AnlJobService;

/**
 * 배치작업 - 분석/재판별 배치 등록·조회, 실행 이력 조회. 전부 로그인이 필요하다.
 * 실행 시작/완료/실패 처리(startExecution/completeExecution/failExecution)는 AnlJobService 에만 있고
 * 이 Controller 는 그걸 직접 호출하지 않는다 - AnlJobScheduler(@Scheduled)가 READY 상태 작업을 자동으로
 * 찾아 DomainPredictJobExecutor 로 실행하는 게 기본 흐름이고, 이 Controller 는 등록/조회와 함께
 * "스케줄러를 기다리지 않고 지금 바로 실행"(runJob)만 예외적으로 제공한다.
 */
@Tag(name = "AnalysisJob", description = "배치작업 - 분석/재판별 배치 등록·조회, 즉시 실행 및 실행 이력 조회")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class AnlJobController {

    private final AnlJobService anlJobService;
    private final DomainPredictJobExecutor domainPredictJobExecutor;

    @Operation(
            summary = "배치작업 등록",
            description = "데이터셋에 대한 분석/재판별 배치작업을 신규 등록한다. 등록자는 인증 정보에서 자동으로 채워지고, "
                    + "등록 직후 작업 상태는 항상 READY 이다."
    )
    @PostMapping
    public ResponseEntity<AnlJobRsp> createJob(
            @Valid @RequestBody AnlJobCreateReq req,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(anlJobService.createJob(req, actor.getUserId()));
    }

    @Operation(summary = "상태별 배치작업 목록 조회", description = "지정한 실행 상태의 배치작업 목록을 조회한다.")
    @GetMapping
    public ResponseEntity<List<AnlJobRsp>> findJobsByStatus(
            @Parameter(description = "배치작업 실행 상태", example = "READY")
            @RequestParam JobStatCd status) {
        return ResponseEntity.ok(anlJobService.findJobsByStatus(status));
    }

    @Operation(
            summary = "배치작업 즉시 실행",
            description = "AnlJobScheduler가 1분마다 READY 상태 작업을 자동으로 찾아 실행하지만, 그걸 기다리지 않고 "
                    + "지금 바로 실행하고 싶을 때 쓴다(Swagger 테스트용으로 특히 유용하다). 데이터셋의 컬럼을 전부 "
                    + "순회하며 AI 판별(POST /api/domain-predictions/columns/{columnId}/predict 와 동일한 로직)을 "
                    + "호출하고, 그 결과(성공/실패 건수)가 담긴 실행 이력을 응답으로 즉시 돌려준다. ROLE_ADMIN 권한이 필요하다."
    )
    @PostMapping("/{jobId}/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnlJobLogRsp> runJob(
            @Parameter(description = "배치작업 ID", example = "1")
            @PathVariable Long jobId,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.ok(domainPredictJobExecutor.execute(jobId, actor.getUserId()));
    }

    @Operation(summary = "데이터셋별 배치작업 목록 조회", description = "지정한 데이터셋에 등록된 배치작업 목록을 조회한다.")
    @GetMapping("/dataset/{datasetId}")
    public ResponseEntity<List<AnlJobRsp>> findJobsByDataset(
            @Parameter(description = "데이터셋 ID", example = "DS0000000001")
            @PathVariable String datasetId) {
        return ResponseEntity.ok(anlJobService.findJobsByDataset(datasetId));
    }

    @Operation(summary = "배치작업 실행 이력 조회", description = "지정한 배치작업의 실행 로그(시작/완료/실패 이력)를 최신순으로 조회한다.")
    @GetMapping("/{jobId}/logs")
    public ResponseEntity<List<AnlJobLogRsp>> findLogsByJob(
            @Parameter(description = "배치작업 ID", example = "1")
            @PathVariable Long jobId) {
        return ResponseEntity.ok(anlJobService.findLogsByJob(jobId));
    }
}
