package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.type.UsrStatCd;

/** 사용자 정보 응답 (비밀번호 해시는 절대 담지 않는다) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsrRsp {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "로그인 아이디", example = "cyj0703")
    private String loginId;

    @Schema(description = "사용자 이름", example = "최윤정")
    private String userName;

    @Schema(description = "이메일", example = "cyjjeong98@gmail.com")
    private String email;

    @Schema(description = "휴대폰 번호", example = "010-1234-5678")
    private String phoneNo;

    @Schema(description = "소속 부서명", example = "개발팀")
    private String deptName;

    @Schema(description = "계정 상태 - ACTIVE(정상)/LOCKED(잠김)/WITHDRAWN(탈퇴)", example = "ACTIVE")
    private UsrStatCd userStatus;

    @Schema(description = "마지막 로그인 일시", example = "2026-08-26T09:00:00")
    private LocalDateTime lastLoginAt;

    @Schema(description = "가입일시", example = "2026-08-01T10:00:00")
    private LocalDateTime createdAt;

    public static UsrRsp from(AppUsr usr) {
        return new UsrRsp(
                usr.getUserId(),
                usr.getLoginId(),
                usr.getUserName(),
                usr.getEmail(),
                usr.getPhoneNo(),
                usr.getDeptName(),
                usr.getUserStatus(),
                usr.getLastLoginAt(),
                usr.getCreatedAt()
        );
    }
}
