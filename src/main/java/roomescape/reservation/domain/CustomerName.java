package roomescape.reservation.domain;

public class CustomerName {

    private final String name;

    private CustomerName(final String value) {
        validate(value);
        this.name = value;
    }

    public static CustomerName from(final String value) {
        return new CustomerName(value);
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해야 합니다.");
        }
    }

    public String getName() {
        return name;
    }
}
