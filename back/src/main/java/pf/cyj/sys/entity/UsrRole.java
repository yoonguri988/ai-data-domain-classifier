package pf.cyj.sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/** USER_ROLE - 사용자-권한 매핑(N:M) */
@Entity
@Table(name = "USER_ROLE")
@IdClass(UsrRoleId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsrRole {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Id
    @Column(name = "ROLE_ID")
    private Long roleId;

    @CreationTimestamp
    @Column(name = "GRANTED_AT", nullable = false, updatable = false)
    private LocalDateTime grantedAt;
}
