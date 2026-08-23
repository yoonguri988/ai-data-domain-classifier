package pf.cyj.sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import pf.cyj.sys.entity.type.NotiChnl;
import pf.cyj.sys.entity.type.SendStatCd;

/** NOTIFICATION_LOG - 승인요청/승인완료 알림 발송이력 (spring-boot-starter-mail, coolsms sdk) */
@Entity
@Table(name = "NOTIFICATION_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_ID")
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "CHANNEL", nullable = false, length = 10)
    private NotiChnl channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TARGET_USER_ID", nullable = false)
    private AppUsr targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RELATED_REQUEST_ID")
    private StdDmnReq stdDmnReq;

    @Column(name = "TITLE", length = 200)
    private String title;

    @Column(name = "CONTENT", length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEND_STATUS", nullable = false, length = 10)
    @Builder.Default
    private SendStatCd sendStatus = SendStatCd.SUCCESS;

    @CreationTimestamp
    @Column(name = "SENT_AT", nullable = false, updatable = false)
    private LocalDateTime sentAt;
}
