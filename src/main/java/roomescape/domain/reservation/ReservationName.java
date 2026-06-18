package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

import static roomescape.domain.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.DomainPreconditions.require;
import static roomescape.domain.DomainPreconditions.requireNonBlank;

@Embeddable
public class ReservationName {
    private static final int MAX_NAME_LENGTH = 20;

    @Column(name = "name", nullable = false)
    private String value;

    protected ReservationName() {
    }

    public ReservationName(String value) {
        requireNonBlank(value, INVALID_INPUT, "예약자 이름은 비어있을 수 없습니다.");
        String striped = value.strip();
        require(isValidLength(striped), INVALID_INPUT, "이름 길이는 1자 ~ 20자 사이여야 합니다.");
        this.value = striped;
    }

    private boolean isValidLength(String value) {
        return value.length() <= MAX_NAME_LENGTH;
    }

    public String getValue() {
        return value;
    }

    public boolean isSame(ReservationName other) {
        return value.equals(other.value);
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
