package pf.cyj.sys.exception;

/**
 * 외부 AI API 호출 실패(네트워크 오류, 4xx/5xx 응답, 응답 파싱 실패, AI가 알 수 없는 도메인 코드를 반환한 경우 등) 시 던진다.
 * ANALYSIS_JOB_LOG 가 구분하는 INTERNAL_ERROR(우리 로직 문제) / CALL_ERROR(외부 API 문제) 중
 * CALL_ERROR 쪽에 대응한다 — 장애 발생 시 로그만 보고 원인을 즉시 구분하기 위한 목적의 예외다.
 * GlobalExceptionHandler 가 502 Bad Gateway + errorCode 로 변환한다.
 */
public class ExternalApiException extends RuntimeException {

    private final String errorCode;

    public ExternalApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
