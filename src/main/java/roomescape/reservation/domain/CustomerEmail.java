package roomescape.reservation.domain;

public record CustomerEmail(String email) {

    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    public CustomerEmail {
        validate(email);
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해야 합니다.");
        }
        if (!value.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("올바른 이메일 형식이어야 합니다.");
        }
    }
}
