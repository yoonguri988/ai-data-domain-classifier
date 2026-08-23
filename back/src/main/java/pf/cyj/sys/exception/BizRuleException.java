package pf.cyj.sys.exception;

/**
 * 업무 규칙 위반(로그인 아이디/이메일 중복, 이미 처리된 승인 건 재처리 등) 시 던진다.
 * GlobalExceptionHandler 가 409 Conflict + errorCode 로 변환한다.
 */
public class BizRuleException extends RuntimeException {

    private final String errorCode;

    public BizRuleException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
