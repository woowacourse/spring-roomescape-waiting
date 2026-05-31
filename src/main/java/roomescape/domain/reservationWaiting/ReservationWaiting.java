package roomescape.domain.reservationWaiting;

import java.time.LocalDateTime;
import roomescape.domain.reservation.Reservation;
import roomescape.exception.InvalidInputException;

public class ReservationWaiting {

    private final Long id;
    private final String name;
    private final Reservation reservation;
    private final Long sequence;
    private final LocalDateTime createdAt;

    private ReservationWaiting(Long id, String name, Reservation reservation, Long sequence, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.reservation = reservation;
        this.sequence = sequence;
        this.createdAt = createdAt;
    }

    public static ReservationWaiting create(String name, Reservation reservation) {
        validateWaitableReservation(name, reservation);
        return new ReservationWaiting(null, name, reservation, null, LocalDateTime.now());
    }

    public static ReservationWaiting restore(Long id, String name, Reservation reservation, Long sequence, LocalDateTime createdAt) {
        return new ReservationWaiting(id, name, reservation, sequence, createdAt);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getSequence() {
        return sequence;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private static void validateWaitableReservation(String name, Reservation reservation) {
        if(reservation.isExpired()) {
            throw new InvalidInputException("이미 지난 예약에 대기열을 등록할 수 없습니다.");
        }

        if(reservation.isReservedBy(name)) {
            throw new InvalidInputException("이미 등록된 예약이 있습니다.");
        }
    }
}
