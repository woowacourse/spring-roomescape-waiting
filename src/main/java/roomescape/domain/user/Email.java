package roomescape.domain.user;

import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_REGEX_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public Email {
        validate(value);
    }

    private void validate(final String value) {
        if (!EMAIL_REGEX_PATTERN.matcher(value)
                .matches()) {
            throw new IllegalArgumentException(String.format("%s는 올바른 이메일 형식이 아닙니다.", value));
        }
    }
}
