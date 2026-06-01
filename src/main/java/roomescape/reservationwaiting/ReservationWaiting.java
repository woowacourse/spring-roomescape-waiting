package roomescape.reservationwaiting;

import java.time.LocalDateTime;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.reservation.Reservation;

public class ReservationWaiting {
    private final Long id;
    private final Reservation reservation;
    private final String name;
    private final LocalDateTime requestAt;

    public ReservationWaiting(Long id, Reservation reservation, String name, LocalDateTime requestAt) {
        this.id = id;
        this.reservation = reservation;
        this.name = validateName(name);
        this.requestAt = requestAt;
    }

    public static ReservationWaiting createNew(final Reservation reservation, String name, LocalDateTime requestAt) {
        return new ReservationWaiting(null, reservation, name, requestAt);
    }

    public ReservationWaiting withId(final Long id) {
        return new ReservationWaiting(id, this.reservation, this.name, this.requestAt);
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getRequestAt() {
        return requestAt;
    }

    private String validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약자 이름은 필수입니다.");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() >= 10) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약자 이름은 10자 미만이어야 합니다.");
        }

        return trimmedName;
    }
}
