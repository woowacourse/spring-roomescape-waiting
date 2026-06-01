package roomescape.domain.reservation;

public class ReservationName {
    private static final int MAX_LENGTH = 10;

    private final String value;

    private ReservationName(final String name) {
        validate(name);
        this.value = name.trim();
    }

    public static ReservationName from(final String name) {
        return new ReservationName(name);
    }

    public String value() {
        return value;
    }

    private void validate(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수입니다.");
        }

        if (name.trim().length() >= MAX_LENGTH) {
            throw new IllegalArgumentException("예약자 이름은 10자 미만이어야 합니다.");
        }
    }
}
