package roomescape.reservation.domain;

public record CustomerEmail(String email) {

    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private static final String EMAIL_REQUIRED_MESSAGE = "이메일을 입력해야 합니다.";
    private static final String INVALID_EMAIL_FORMAT_MESSAGE = "올바른 이메일 형식이어야 합니다.";

    public CustomerEmail {
        validate(email);
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(EMAIL_REQUIRED_MESSAGE);
        }
        if (!value.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException(INVALID_EMAIL_FORMAT_MESSAGE);
        }
    }
}
