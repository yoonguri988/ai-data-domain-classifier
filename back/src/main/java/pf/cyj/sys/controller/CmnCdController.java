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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import pf.cyj.sys.dto.request.CmnCdGrpSaveReq;
import pf.cyj.sys.dto.request.CmnCdSaveReq;
import pf.cyj.sys.dto.response.CmnCdGrpRsp;
import pf.cyj.sys.dto.response.CmnCdRsp;
import pf.cyj.sys.service.CmnCdService;

/**
 * 공통코드 - 그룹/상세 등록(upsert)은 관리자만, 조회는 SecurityConfig 에서 GET "/api/common-codes/**" 를
 * 비로그인으로 열어둔다(화면 초기 로딩용 콤보박스 등).
 * 등록/수정 API 는 같은 키(codeGroup, 또는 codeGroup+codeValue)로 다시 호출하면 새로 만들지 않고 값만
 * 갱신한다(upsert) - Swagger UI 에서 같은 codeGroup 으로 여러 번 호출해봐도 중복이 생기지 않는다.
 */
@Tag(name = "CommonCode", description = "공통코드 - 코드그룹/상세 등록(관리자) 및 조회(비로그인 허용)")
@RestController
@RequestMapping("/api/common-codes")
@RequiredArgsConstructor
public class CmnCdController {

    private final CmnCdService cmnCdService;

    @Operation(
            summary = "공통코드 그룹 등록/수정 (upsert)",
            description = "코드그룹을 신규 등록하거나 이미 존재하면 그룹명만 갱신한다. ROLE_ADMIN 권한이 필요하다."
    )
    @PostMapping("/groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CmnCdGrpRsp> saveGrp(@Valid @RequestBody CmnCdGrpSaveReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cmnCdService.saveGrp(req));
    }

    @Operation(summary = "공통코드 그룹 전체 조회", description = "등록된 모든 코드그룹을 조회한다. 비로그인으로도 호출 가능하다.")
    @GetMapping("/groups")
    public ResponseEntity<List<CmnCdGrpRsp>> findAllGrp() {
        return ResponseEntity.ok(cmnCdService.findAllGrp());
    }

    @Operation(
            summary = "공통코드 상세 등록/수정 (upsert)",
            description = "코드그룹 하위의 상세 코드를 신규 등록하거나 (codeGroup, codeValue) 가 이미 존재하면 나머지 값만 "
                    + "갱신한다. ROLE_ADMIN 권한이 필요하고, codeGroup 은 미리 등록된 그룹이어야 한다."
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CmnCdRsp> saveCd(@Valid @RequestBody CmnCdSaveReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cmnCdService.saveCd(req));
    }

    @Operation(summary = "그룹별 공통코드 상세 조회", description = "지정한 코드그룹에 속한 상세 코드 목록을 정렬순서 오름차순으로 조회한다. 비로그인으로도 호출 가능하다.")
    @GetMapping
    public ResponseEntity<List<CmnCdRsp>> findCdByGrp(
            @Parameter(description = "조회할 코드 그룹", example = "JOB_STAT_CD")
            @RequestParam(value="codeGroup") String codeGroup,
            @Parameter(description = "true 면 사용중(useYn=true)인 코드만 필터링해서 반환", example = "true")
            @RequestParam(value="useOnly", defaultValue = "true") boolean useOnly) {
        return ResponseEntity.ok(cmnCdService.findCdByGrp(codeGroup, useOnly));
    }
}
