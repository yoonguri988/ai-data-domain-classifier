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
import pf.cyj.sys.dto.request.StdDmnReqCreateReq;
import pf.cyj.sys.dto.request.StdDmnReqReviewReq;
import pf.cyj.sys.dto.response.StdDmnReqRsp;
import pf.cyj.sys.dto.response.StdDmnRsp;
import pf.cyj.sys.oauth2.CustomUserPrincipal;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.service.StdDmnService;

/**
 * 표준도메인승인 - 확정 신청은 로그인한 누구나, 대기 목록 조회와 승인/반려는 관리자만
 * (@PreAuthorize("hasRole('ADMIN')"), SecurityConfig 의 @EnableMethodSecurity 로 동작).
 * 확정본(StdDmn) 조회는 로그인만 필요하다.
 */
@RestController
@RequiredArgsConstructor
public class StdDmnController {

    private final StdDmnService stdDmnService;

    @PostMapping("/api/std-domain-requests")
    public ResponseEntity<StdDmnReqRsp> applyRequest(
            @Valid @RequestBody StdDmnReqCreateReq req,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(stdDmnService.applyRequest(req, actor.userId()));
    }

    @GetMapping("/api/std-domain-requests/mine")
    public ResponseEntity<List<StdDmnReqRsp>> findMyRequests(@AuthenticationPrincipal CustomUserPrincipal principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.ok(stdDmnService.findRequestsByRequester(actor.userId()));
    }

    @GetMapping("/api/std-domain-requests/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StdDmnReqRsp>> findPendingRequests() {
        return ResponseEntity.ok(stdDmnService.findPendingRequests());
    }

    @PatchMapping("/api/std-domain-requests/{requestId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StdDmnReqRsp> review(
            @PathVariable Long requestId,
            @Valid @RequestBody StdDmnReqReviewReq req,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ActorContext actor = ActorContext.from(principal);
        return ResponseEntity.ok(stdDmnService.review(requestId, req, actor.userId()));
    }

    @GetMapping("/api/std-domains/columns/{columnId}")
    public ResponseEntity<StdDmnRsp> findConfirmedByColumn(@PathVariable Long columnId) {
        return ResponseEntity.ok(stdDmnService.findConfirmedByColumn(columnId));
    }
}
