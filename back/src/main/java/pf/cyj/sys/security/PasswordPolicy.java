package pf.cyj.sys.security;

import java.util.regex.Pattern;

/**
 * 비밀번호 정책 검증 유틸.
 * - 최소 8자 이상, 영문 + 숫자 + 특수문자 조합 모두 필수 (SignupReq 의 @Pattern 과 동일한 규칙)
 * - 회원가입은 Bean Validation(@Pattern)으로 검증하므로 이 클래스를 거치지 않는다.
 *   비밀번호 변경/재설정처럼 검증 시점이 여러 곳으로 흩어질 수 있는 기능이 추가될 때,
 *   Service 계층에서 PasswordPolicy.validate(...)를 호출해 재사용하기 위해 미리 준비해 둔다.
 * - 검증 실패 시 IllegalArgumentException 을 던지며, GlobalExceptionHandler 가 400 Bad Request 로 변환한다.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    private static final Pattern HAS_LETTER = Pattern.compile("[A-Za-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]");

    private PasswordPolicy() {
    }

    public static void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
        }
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 " + MIN_LENGTH + "자 이상이어야 합니다.");
        }
        if (!HAS_LETTER.matcher(password).find()) {
            throw new IllegalArgumentException("비밀번호에 영문을 포함해주세요.");
        }
        if (!HAS_DIGIT.matcher(password).find()) {
            throw new IllegalArgumentException("비밀번호에 숫자를 포함해주세요.");
        }
        if (!HAS_SPECIAL.matcher(password).find()) {
            throw new IllegalArgumentException("비밀번호에 특수문자를 포함해주세요.");
        }
    }

    public static boolean isValid(String password) {
        try {
            validate(password);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
