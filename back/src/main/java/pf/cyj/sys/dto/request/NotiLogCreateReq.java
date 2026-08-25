package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** NOTIFICATION_LOG 등록 요청 - 승인요청/승인완료 등 알림 발송이력 기록 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotiLogCreateReq {

    @NotBlank(message = "채널은 필수입니다.")
    private String channel;

    @NotNull(message = "수신 대상 사용자 ID는 필수입니다.")
    private Long targetUserId;

    private Long relatedRequestId;

    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String content;
}
