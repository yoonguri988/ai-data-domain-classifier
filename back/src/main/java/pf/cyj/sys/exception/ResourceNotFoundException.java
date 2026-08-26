package pf.cyj.sys.exception;

/** 요청한 리소스(엔티티)를 찾을 수 없을 때 던진다. GlobalExceptionHandler 가 404 Not Found 로 변환한다. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
