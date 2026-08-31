package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.type.UsrStatCd;

/**
 * 관리자용 사용자 목록/상세 응답 - {@link UsrRsp}와 달리 roles 를 포함한다. 로그인 응답(UsrRsp)에는 일부러
 * roles 를 안 넣었지만(프론트가 Access Token 클레임에서 디코딩해서 쓴다 - AuthAcntService.issueTokens 참고),
 * 관리자 화면에서 "이 사용자가 지금 어떤 권한을 가지고 있는지" 보려면 DB에 실제로 부여된 역할을 그대로
 * 보여줘야 해서 별도 DTO로 뒀다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsrAdmRsp {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "로그인 아이디", example = "cyj0703")
    private String loginId;

    @Schema(description = "사용자 이름", example = "최윤정")
    private String userName;

    @Schema(description = "이메일", example = "cyjjeong98@gmail.com")
    private String email;

    @Schema(description = "소속 부서명", example = "개발팀")
    private String deptName;

    @Schema(description = "계정 상태", example = "ACTIVE")
    private UsrStatCd userStatus;

    @Schema(description = "현재 부여된 권한 코드 목록", example = "[\"ROLE_USER\", \"ROLE_REVIEWER\"]")
    private List<String> roles;

    @Schema(description = "마지막 로그인 일시", example = "2026-08-31T09:00:00")
    private LocalDateTime lastLoginAt;

    @Schema(description = "가입일시", example = "2026-08-01T10:00:00")
    private LocalDateTime createdAt;

    public static UsrAdmRsp from(AppUsr usr, List<String> roles) {
        return new UsrAdmRsp(
                usr.getUserId(),
                usr.getLoginId(),
                usr.getUserName(),
                usr.getEmail(),
                usr.getDeptName(),
                usr.getUserStatus(),
                roles,
                usr.getLastLoginAt(),
                usr.getCreatedAt()
        );
    }
}
