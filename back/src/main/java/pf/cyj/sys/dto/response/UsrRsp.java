package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.type.UsrStatCd;

/** 사용자 정보 응답 (비밀번호 해시는 제외) */
public record UsrRsp(
        Long userId,
        String loginId,
        String userName,
        String email,
        String phoneNo,
        String deptName,
        UsrStatCd userStatus,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
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
