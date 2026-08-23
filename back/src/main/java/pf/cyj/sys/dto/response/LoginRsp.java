package pf.cyj.sys.dto.response;

/** 로그인 성공 응답 - JWT Access/Refresh Token 발급 결과 */
public record LoginRsp(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UsrRsp user
) {
    public static LoginRsp of(String accessToken, String refreshToken, long expiresIn, UsrRsp user) {
        return new LoginRsp(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
