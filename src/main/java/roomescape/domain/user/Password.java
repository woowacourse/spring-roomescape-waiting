package roomescape.domain.user;

public record Password(String value) {
    private static final int MIN_LENGTH = 8;
    private static final String ERROR_MESSAGE = String.format("비밀번호는 %d 글자 이상 이여야 합니다.", MIN_LENGTH);

    public Password {
        validate(value);
    }

    public boolean isEqual(final String password) {
        return this.value.equals(password);
    }

    private void validate(final String value) {
        if (value.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(ERROR_MESSAGE);
        }
    }
}
