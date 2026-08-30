package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 알림 발송 이력(NOTIFICATION_LOG) 등록 요청 - 승인요청/승인완료 등 알림을 이메일로 실제 발송하고
 * 그 결과를 이력으로 남긴다. 채널은 현재 이메일만 지원해서(NotiChnl 참고) 요청 바디에 따로 받지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotiLogCreateReq {

    @Schema(description = "수신 대상 사용자 ID", example = "1")
    @NotNull(message = "수신 대상 사용자 ID는 필수입니다.")
    private Long targetUserId;

    @Schema(description = "관련된 표준 도메인 확정 신청 ID (선택)", example = "1")
    private Long relatedRequestId;

    @Schema(description = "알림 제목", example = "표준 도메인 확정 승인 요청")
    @Size(max = 200)
    private String title;

    @Schema(description = "알림 본문", example = "확인이 필요한 승인 요청이 있습니다.")
    @Size(max = 1000)
    private String content;
}
