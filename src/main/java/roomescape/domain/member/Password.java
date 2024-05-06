package roomescape.domain.member;

public class Password {
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 20;

    private final String password;

    public Password(String password) {
        validateNonBlank(password);
        validateLength(password);
        this.password = password;
    }

    private static void validateNonBlank(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력값 입니다.");
        }
    }

    private static void validateLength(String rawPassword) {
        if (rawPassword.length() < PASSWORD_MIN_LENGTH || rawPassword.length() > PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 8자 이상 20자 이하여야 합니다.");
        }
    }


    public boolean matches(String other) {
        return password.equals(other);
    }

    public String getPassword() {
        return password;
    }
}
