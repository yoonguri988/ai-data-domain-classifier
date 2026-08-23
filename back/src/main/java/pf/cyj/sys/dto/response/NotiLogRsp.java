package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import pf.cyj.sys.entity.NotiLog;
import pf.cyj.sys.entity.type.NotiChnl;
import pf.cyj.sys.entity.type.SendStatCd;

public record NotiLogRsp(
        Long notificationId,
        NotiChnl channel,
        Long targetUserId,
        String targetUserName,
        Long relatedRequestId,
        String title,
        String content,
        SendStatCd sendStatus,
        LocalDateTime sentAt
) {
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
