package pf.cyj.sys.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import pf.cyj.sys.dto.request.StdDmnReqCreateReq;
import pf.cyj.sys.dto.request.StdDmnReqReviewReq;
import pf.cyj.sys.dto.response.StdDmnReqRsp;
import pf.cyj.sys.dto.response.StdDmnRsp;
import pf.cyj.sys.oauth2.CustomOAuth2User;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.service.StdDmnService;

/**
 * 표준도메인승인 - 확정 신청은 로그인한 누구나, 대기 목록 조회와 승인/반려는 관리자(ROLE_ADMIN) 또는
 * 표준 도메인 승인자(ROLE_REVIEWER)만 (@PreAuthorize("hasAnyRole('ADMIN','REVIEWER')"), SecurityConfig 의
 * @EnableMethodSecurity 로 동작). ROLE_REVIEWER 는 시드 데이터의 reviewer1(김승인) 계정처럼 승인 업무만
 * 전담하는 담당자를 위한 역할이다 - ROLE_ADMIN 만 허용하면 이 역할이 있을 이유가 없어져서 함께 열어뒀다.
 * 확정본(StdDmn) 조회는 로그인만 필요하다.
 *
 * <p>Swagger UI 테스트 순서 예시: (1) POST /api/std-domain-requests 로 확정 신청(PENDING 상태로 생성)
 * → (2) 관리자 또는 reviewer1 계정으로 PATCH /api/std-domain-requests/{requestId}/review 호출해
 * 승인(approve=true) → (3) GET /api/std-domains/columns/{columnId} 로 확정본이 생성됐는지 확인.
 */
@Tag(name = "StandardDomainApproval", description = "표준도메인승인 - 확정 신청/승인/반려 및 확정본 조회")
@RestController
@RequiredArgsConstructor
public class StdDmnController {

    private final StdDmnService stdDmnService;

    @Operation(summary = "표준 도메인 확정 신청", description = "AI 판별 결과를 근거로 컬럼의 표준 도메인 확정을 신청한다(상태 PENDING). 로그인한 사용자면 누구나 신청할 수 있다.")
    @PostMapping("/api/std-domain-requests")
    public ResponseEntity<StdDmnReqRsp> applyRequest(
            @Valid @RequestBody StdDmnReqCreateReq req,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(stdDmnService.applyRequest(req, actor.getUserId()));
    }

    @Operation(summary = "내 확정 신청 목록 조회", description = "현재 로그인한 사용자가 신청한 표준 도메인 확정 요청 목록을 조회한다.")
    @GetMapping("/api/std-domain-requests/mine")
    public ResponseEntity<List<StdDmnReqRsp>> findMyRequests(@AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.ok(stdDmnService.findRequestsByRequester(actor.getUserId()));
    }

    @Operation(summary = "승인 대기 신청 목록 조회", description = "아직 승인/반려 처리되지 않은(PENDING) 확정 신청 목록을 조회한다. ROLE_ADMIN 또는 ROLE_REVIEWER 권한이 필요하다.")
    @GetMapping("/api/std-domain-requests/pending")
    @PreAuthorize("hasAnyRole('ADMIN','REVIEWER')")
    public ResponseEntity<List<StdDmnReqRsp>> findPendingRequests() {
        return ResponseEntity.ok(stdDmnService.findPendingRequests());
    }

    @Operation(
            summary = "확정 신청 승인/반려",
            description = "대기 중인 확정 신청 건을 승인 또는 반려 처리한다. 승인 시 표준 도메인 확정본(StdDmn)이 "
                    + "생성/갱신된다(기존 확정본이 있으면 versionNo 가 1 증가). 승인/반려 어느 쪽이든 처리 즉시 "
                    + "신청자에게 이메일 알림이 자동 발송되고 그 이력이 GET /api/notifications/mine 에 남는다"
                    + "(발송이 실패해도 승인/반려 처리 자체는 롤백되지 않는다). ROLE_ADMIN 또는 ROLE_REVIEWER "
                    + "권한이 필요하다."
    )
    @PatchMapping("/api/std-domain-requests/{requestId}/review")
    @PreAuthorize("hasAnyRole('ADMIN','REVIEWER')")
    public ResponseEntity<StdDmnReqRsp> review(
            @Parameter(description = "확정 신청 ID", example = "1")
            @PathVariable("requestId") Long requestId,
            @Valid @RequestBody StdDmnReqReviewReq req,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.ok(stdDmnService.review(requestId, req, actor.getUserId()));
    }

    @Operation(summary = "컬럼별 확정 표준 도메인 조회", description = "지정한 컬럼에 확정된 표준 도메인 정보를 조회한다(확정본이 없으면 404).")
    @GetMapping("/api/std-domains/columns/{columnId}")
    public ResponseEntity<StdDmnRsp> findConfirmedByColumn(
            @Parameter(description = "분석 컬럼 ID", example = "1")
            @PathVariable("columnId") Long columnId) {
        return ResponseEntity.ok(stdDmnService.findConfirmedByColumn(columnId));
    }
}
