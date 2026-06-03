package roomescape.reservation.domain;

public record CustomerName(String name) {

    private static final int MAX_LENGTH = 10;

    public CustomerName {
        validate(name);
    }

    private void validate(final String value) {
        validateRequireNotBlank(value);
        validateLength(value);
    }

    private void validateRequireNotBlank(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해야 합니다.");
        }
    }

    private void validateLength(final String value) {
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("이름은 10글자 이하이어야 합니다.");
        }
    }
}
