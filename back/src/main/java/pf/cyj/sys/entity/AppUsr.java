package pf.cyj.sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pf.cyj.sys.entity.type.UsrStatCd;

/** APP_USER - 애플리케이션 사용자 계정 */
@Entity
@Table(name = "APP_USER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUsr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "LOGIN_ID", nullable = false, length = 50, unique = true)
    private String loginId;

    /** OAuth2 전용 계정은 NULL 허용 (BCrypt 해시 저장) */
    @Column(name = "PASSWORD", length = 200)
    private String password;

    @Column(name = "USER_NAME", nullable = false, length = 100)
    private String userName;

    @Column(name = "EMAIL", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "PHONE_NO", length = 20)
    private String phoneNo;

    @Column(name = "DEPT_NAME", length = 100)
    private String deptName;

    @Enumerated(EnumType.STRING)
    @Column(name = "USER_STATUS", nullable = false, length = 10)
    @Builder.Default
    private UsrStatCd userStatus = UsrStatCd.ACTIVE;

    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;
}
