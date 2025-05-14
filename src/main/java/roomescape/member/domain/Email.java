package roomescape.member.domain;

import java.util.regex.Pattern;

import jakarta.persistence.Embeddable;

@Embeddable
public record Email(String email) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public Email {
        validate(email);
    }

    private void validate(final String email) {
        validateBlank(email);
        validateEmailPattern(email);
    }

    private void validateBlank(final String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("값이 존재하지 않습니다.");
        }
    }

    private void validateEmailPattern(final String email) {
        if (!EMAIL_PATTERN.matcher(email)
                .matches()
        ) {
            throw new IllegalArgumentException("%s는 이메일 형식이 아닙니다.".formatted(email));
        }
    }
}
