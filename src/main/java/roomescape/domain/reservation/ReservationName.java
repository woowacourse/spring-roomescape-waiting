package roomescape.domain.reservation;

import roomescape.common.exception.BadRequestException;

import java.util.Objects;

public class ReservationName {
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 20;

    private final String value;

    public ReservationName(String value) {
        Objects.requireNonNull(value);
        String striped = value.strip();
        validateLength(striped);
        this.value = striped;
    }

    public void validateLength(String value) {
        if (value.length() < MIN_NAME_LENGTH || value.length() > MAX_NAME_LENGTH) {
            throw new BadRequestException("이름 길이는 1자 ~ 20자 사이여야 합니다.");
        }
    }

    public String getValue() {
        return value;
    }

    public boolean isSame(String other) {
        return value.equals(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationName reservationName = (ReservationName) o;
        return Objects.equals(value, reservationName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
