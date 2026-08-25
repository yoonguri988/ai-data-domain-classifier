package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.type.UsrStatCd;

/** 사용자 정보 응답 (비밀번호 해시는 제외) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsrRsp {

    private Long userId;
    private String loginId;
    private String userName;
    private String email;
    private String phoneNo;
    private String deptName;
    private UsrStatCd userStatus;
    private LocalDateTime lastLoginAt;
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
