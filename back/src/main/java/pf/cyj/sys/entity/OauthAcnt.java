package pf.cyj.sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/** OAUTH_ACCOUNT - OAuth2.0 소셜 로그인 연동 계정 */
@Entity
@Table(name = "OAUTH_ACCOUNT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OauthAcnt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OAUTH_ACCOUNT_ID")
    private Long oauthAcntId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private AppUsr appUsr;

    /** GOOGLE, GITHUB, KAKAO ... */
    @Column(name = "PROVIDER", nullable = false, length = 20)
    private String provider;

    @Column(name = "PROVIDER_USER_ID", nullable = false, length = 100)
    private String providerUserId;

    @CreationTimestamp
    @Column(name = "LINKED_AT", nullable = false, updatable = false)
    private LocalDateTime linkedAt;
}
