package roomescape.member.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public class Email {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9+-_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";

    private final String value;

    public Email(String value) {
        validate(value);

        this.value = value;
    }

    private void validate(String value) {
        if (value == null) {
            throw new IllegalArgumentException("[ERROR] 이메일이 없습니다.");
        }
        if (!Pattern.matches(EMAIL_REGEX, value)) {
            throw new IllegalArgumentException("[ERROR] 이메일 형식이 아닙니다.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Email that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
