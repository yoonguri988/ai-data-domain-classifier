package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 사용자 권한 부여/회수 요청 - grant=true 면 해당 roleCode 를 부여, false 면 회수한다. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsrRoleUpdateReq {

    @Schema(description = "부여/회수할 역할 코드 (미리 등록된 역할이어야 함)", example = "ROLE_REVIEWER")
    @NotBlank(message = "역할 코드는 필수입니다.")
    private String roleCode;

    @Schema(description = "true 면 부여, false 면 회수", example = "true")
    @NotNull(message = "grant 는 필수입니다.")
    private Boolean grant;
}
