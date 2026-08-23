package pf.cyj.sys.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** USER_ROLE 복합키 (USER_ID + ROLE_ID) - UsrRole 의 @IdClass 로 사용 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsrRoleId implements Serializable {

    private Long userId;
    private Long roleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsrRoleId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}
