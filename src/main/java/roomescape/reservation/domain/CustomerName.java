package roomescape.reservation.domain;

public record CustomerName(String name) {

    public CustomerName {
        validate(name);
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해야 합니다.");
        }
    }
}
