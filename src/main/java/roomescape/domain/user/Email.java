package roomescape.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record Email(
        @Column(name = "email") String address) {
    private static final Pattern EMAIL_REGEX_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public Email {
        validate(address);
    }

    private void validate(final String value) {
        if (!EMAIL_REGEX_PATTERN.matcher(value)
                .matches()) {
            throw new IllegalArgumentException(String.format("%s는 올바른 이메일 형식이 아닙니다.", value));
        }
    }
}
