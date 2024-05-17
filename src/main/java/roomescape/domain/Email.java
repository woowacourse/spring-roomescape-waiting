package roomescape.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public class Email {
    private static final String REGEX = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private String value;

    public Email() {
    }

    public Email(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String email) {
        validateBlank(email);
        validateFormat(email);
    }

    private void validateBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수로 입력해야 합니다.");
        }
    }

    private void validateFormat(String email) {
        if (!PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("email 형식에 맞지 않습니다.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
