package roomescape.reservation.domain;

public record CustomerName(String name) {

    private static final int MAX_LENGTH = 10;

    public CustomerName {
        validate(name);
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해야 합니다.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("이름은 10자 이하여야 합니다.");
        }
    }
}
