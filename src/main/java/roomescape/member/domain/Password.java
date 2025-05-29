package roomescape.member.domain;

import java.util.Objects;

public class Password {

    private static final int MIN_PASSWORD_SIZE = 8;
    private static final int MAX_PASSWORD_SIZE = 50;

    private final String value;

    public Password(String value) {
        validate(value);

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private void validate(String value) {
        if (value == null) {
            throw new IllegalArgumentException("[ERROR] 비밀번호가 없습니다.");
        }

        String trimmed = value.trim();
        if (trimmed.length() < MIN_PASSWORD_SIZE || trimmed.length() > MAX_PASSWORD_SIZE) {
            throw new IllegalArgumentException("[ERROR] 비밀번호는 8자 이상, 50자 이하여야 합니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Password that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
