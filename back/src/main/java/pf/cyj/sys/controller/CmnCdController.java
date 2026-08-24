package pf.cyj.sys.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pf.cyj.sys.dto.request.CmnCdGrpSaveReq;
import pf.cyj.sys.dto.request.CmnCdSaveReq;
import pf.cyj.sys.dto.response.CmnCdGrpRsp;
import pf.cyj.sys.dto.response.CmnCdRsp;
import pf.cyj.sys.service.CmnCdService;

/**
 * 공통코드 - 그룹/상세 등록(upsert)은 관리자만, 조회는 SecurityConfig 에서 GET "/api/common-codes/**" 를
 * 비로그인으로 열어둔다(화면 초기 로딩용 콤보박스 등).
 */
@RestController
@RequestMapping("/api/common-codes")
@RequiredArgsConstructor
public class CmnCdController {

    private final CmnCdService cmnCdService;

    @PostMapping("/groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CmnCdGrpRsp> saveGrp(@Valid @RequestBody CmnCdGrpSaveReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cmnCdService.saveGrp(req));
    }

    @GetMapping("/groups")
    public ResponseEntity<List<CmnCdGrpRsp>> findAllGrp() {
        return ResponseEntity.ok(cmnCdService.findAllGrp());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CmnCdRsp> saveCd(@Valid @RequestBody CmnCdSaveReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cmnCdService.saveCd(req));
    }

    @GetMapping
    public ResponseEntity<List<CmnCdRsp>> findCdByGrp(
            @RequestParam String codeGroup,
            @RequestParam(defaultValue = "true") boolean useOnly) {
        return ResponseEntity.ok(cmnCdService.findCdByGrp(codeGroup, useOnly));
    }
}
