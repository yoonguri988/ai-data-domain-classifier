package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Access Token 재발급 요청 (Redis 에 저장된 Refresh Token 검증용) */
public record TknReissueReq(

        @NotBlank(message = "Refresh Token 은 필수입니다.")
        String refreshToken
) {
}
