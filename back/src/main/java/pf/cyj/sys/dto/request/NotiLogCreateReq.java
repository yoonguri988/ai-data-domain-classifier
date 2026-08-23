package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** NOTIFICATION_LOG 등록 요청 - 승인요청/승인완료 등 알림 발송이력 기록 */
public record NotiLogCreateReq(

        @NotBlank(message = "채널은 필수입니다.")
        String channel,

        @NotNull(message = "수신 대상 사용자 ID는 필수입니다.")
        Long targetUserId,

        Long relatedRequestId,

        @Size(max = 200)
        String title,

        @Size(max = 1000)
        String content
) {
}
