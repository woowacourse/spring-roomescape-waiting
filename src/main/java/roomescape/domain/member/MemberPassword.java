package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public record MemberPassword(
        @Column(nullable = false)
        @Size(max = MemberPassword.MAXIMUM_PASSWORD_LENGTH)
        String password
) {

    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    private static final Pattern LETTER_PATTERN = Pattern.compile("[A-Za-z]");
    public static final int MAXIMUM_PASSWORD_LENGTH = 30;
    public static final int MINIMUM_DIGIT_COUNT = 2;
    public static final int MINIMUM_SPECIAL_CHAR_COUNT = 1;
    public static final int MINIMUM_LETTER_COUNT = 5;

    public MemberPassword(final String password) {
        this.password = Objects.requireNonNull(password, "password은 null일 수 없습니다.");
        if (password.isBlank()) {
            throw new IllegalStateException("비밀번호는 공백일 수 없습니다.");
        }
        if (password.length() > MAXIMUM_PASSWORD_LENGTH) {
            throw new IllegalStateException("비밀번호는 30자를 초과할 수 없습니다.");
        }
        validatePasswordPattern(password);
    }

    private void validatePasswordPattern(final String password) {
        long digitCount = DIGIT_PATTERN.matcher(password).results().count();
        long specialCharCount = SPECIAL_CHAR_PATTERN.matcher(password).results().count();
        long letterCount = LETTER_PATTERN.matcher(password).results().count();

        if (digitCount < MINIMUM_DIGIT_COUNT) {
            throw new IllegalStateException("비밀번호에는 숫자가 최소 2자 이상 포함되어야 합니다. (현재: " + digitCount + ")");
        }

        if (specialCharCount < MINIMUM_SPECIAL_CHAR_COUNT) {
            throw new IllegalStateException("비밀번호에는 특수문자가 최소 1자 이상 포함되어야 합니다. (현재: " + specialCharCount + ")");
        }

        if (letterCount < MINIMUM_LETTER_COUNT) {
            throw new IllegalStateException("비밀번호에는 영문자가 최소 5자 이상 포함되어야 합니다. (현재: " + letterCount + ")");
        }
    }
}
