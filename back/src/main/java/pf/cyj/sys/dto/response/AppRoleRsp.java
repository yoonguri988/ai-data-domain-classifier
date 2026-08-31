package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AppRole;

/** 권한(Role) 마스터 응답 - 관리자 화면에서 "이 사용자에게 어떤 역할을 줄지" 고를 콤보박스 등에 쓴다. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppRoleRsp {

    @Schema(description = "역할 ID", example = "2")
    private Long roleId;

    @Schema(description = "역할 코드", example = "ROLE_REVIEWER")
    private String roleCode;

    @Schema(description = "역할명", example = "표준 도메인 승인자")
    private String roleName;

    public static AppRoleRsp from(AppRole role) {
        return new AppRoleRsp(role.getRoleId(), role.getRoleCode(), role.getRoleName());
    }
}
