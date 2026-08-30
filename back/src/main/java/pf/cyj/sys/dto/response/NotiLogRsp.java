package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.NotiLog;
import pf.cyj.sys.entity.type.NotiChnl;
import pf.cyj.sys.entity.type.SendStatCd;

/** 알림 발송 이력(NOTIFICATION_LOG) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotiLogRsp {

    @Schema(description = "알림 이력 ID", example = "1")
    private Long notificationId;

    @Schema(description = "발송 채널 - 현재는 EMAIL만 지원", example = "EMAIL")
    private NotiChnl channel;

    @Schema(description = "수신 대상 사용자 ID", example = "1")
    private Long targetUserId;

    @Schema(description = "수신 대상 사용자 이름", example = "최윤정")
    private String targetUserName;

    @Schema(description = "관련된 표준 도메인 확정 신청 ID (있는 경우만)", example = "1")
    private Long relatedRequestId;

    @Schema(description = "알림 제목", example = "표준 도메인 확정 승인 요청")
    private String title;

    @Schema(description = "알림 본문", example = "확인이 필요한 승인 요청이 있습니다.")
    private String content;

    @Schema(description = "발송 상태 - SUCCESS/FAIL", example = "SUCCESS")
    private SendStatCd sendStatus;

    @Schema(description = "발송 일시", example = "2026-08-26T09:00:00")
    private LocalDateTime sentAt;

    public static NotiLogRsp from(NotiLog noti) {
        return new NotiLogRsp(
                noti.getNotificationId(),
                noti.getChannel(),
                noti.getTargetUser() != null ? noti.getTargetUser().getUserId() : null,
                noti.getTargetUser() != null ? noti.getTargetUser().getUserName() : null,
                noti.getStdDmnReq() != null ? noti.getStdDmnReq().getRequestId() : null,
                noti.getTitle(),
                noti.getContent(),
                noti.getSendStatus(),
                noti.getSentAt()
        );
    }
}
