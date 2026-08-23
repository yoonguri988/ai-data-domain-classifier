package pf.cyj.sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import pf.cyj.sys.entity.converter.YnConverter;

/**
 * REFRESH_TOKEN - JWT Refresh Token 발급/폐기 감사이력.
 * 실서비스에서 TTL 을 갖는 1차 저장소는 Redis이며, 이 테이블은 감사/추적용 백업이다.
 */
@Entity
@Table(name = "REFRESH_TOKEN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfshTkn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TOKEN_ID")
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private AppUsr appUsr;

    /** 토큰 원문 대신 SHA-256 해시만 저장 */
    @Column(name = "TOKEN_HASH", nullable = false, length = 200)
    private String tokenHash;

    @CreationTimestamp
    @Column(name = "ISSUED_AT", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Convert(converter = YnConverter.class)
    @Column(name = "REVOKED_YN", nullable = false, length = 1, columnDefinition = "char(1)")
    @Builder.Default
    private boolean revokedYn = false;

    @Column(name = "CLIENT_IP", length = 45)
    private String clientIp;

    @Column(name = "USER_AGENT", length = 300)
    private String userAgent;
}
