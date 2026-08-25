package pf.cyj.sys.exception;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 전역 예외 처리기 - Service 계층이 던지는 예외를 일관된 JSON 응답(HTTP 상태코드 + 메시지)으로 변환한다. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(null, ex.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuth(AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(BizRuleException.class)
    public ResponseEntity<Map<String, String>> handleBizRule(BizRuleException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(null, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        log.error("[GlobalExceptionHandler] 처리되지 않은 예외 발생", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."));
    }

    private Map<String, String> errorBody(String errorCode, String message) {
        Map<String, String> body = new HashMap<>();
        if (errorCode != null) {
            body.put("errorCode", errorCode);
        }
        body.put("error", message);
        return body;
    }
}
