package roomescape.reservation.domain;

public record CustomerName(String name) {

    private static final int MAX_LENGTH = 10;
    private static final String NAME_REQUIRED_MESSAGE = "이름을 입력해야 합니다.";
    private static final String NAME_MAX_LENGTH_MESSAGE = "이름은 10자 이하여야 합니다.";

    public CustomerName {
        validate(name);
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(NAME_REQUIRED_MESSAGE);
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(NAME_MAX_LENGTH_MESSAGE);
        }
    }
}
