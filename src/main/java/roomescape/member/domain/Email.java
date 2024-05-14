package roomescape.member.domain;

import java.util.regex.Pattern;

public class Email {
    private static final int MAX_EMAIL_LENGTH = 20;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$"
    );

    private final String value;

    public Email(final String value) {
        validateEmail(value);
        this.value = value;
    }

    private void validateEmail(final String value) {
        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("[ERROR] 이메일을 입력해주세요.");
        }
        if (value.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("[ERROR] 이메일은 " + MAX_EMAIL_LENGTH + "자 이하로 입력해주세요.");
        }
        if (isInvalidEmail(value)) {
            throw new IllegalArgumentException("[ERROR] 올바르지 않은 이메일 입력입니다.");
        }
    }

    private boolean isInvalidEmail(final String value) {
        return !EMAIL_PATTERN.matcher(value).matches();
    }

    public String getValue() {
        return value;
    }
}
