package pf.cyj.sys.exception;

/**
 * 인증 실패(아이디/비밀번호 불일치, Refresh Token 무효·만료, 로그인 불가 계정 상태) 시 던진다.
 * GlobalExceptionHandler 가 401 Unauthorized + errorCode 로 변환한다.
 */
public class AuthException extends RuntimeException {

    private final String errorCode;

    public AuthException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
