package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.NotiLog;
import pf.cyj.sys.entity.type.NotiChnl;
import pf.cyj.sys.entity.type.SendStatCd;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotiLogRsp {

    private Long notificationId;
    private NotiChnl channel;
    private Long targetUserId;
    private String targetUserName;
    private Long relatedRequestId;
    private String title;
    private String content;
    private SendStatCd sendStatus;
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
