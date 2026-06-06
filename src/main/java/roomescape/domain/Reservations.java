package roomescape.domain;

import roomescape.exception.BusinessRuleViolationException;

import java.util.List;

public class Reservations {

    private static final String NOT_RESERVED_SLOT = "예약된 슬롯에만 대기를 신청할 수 있습니다.";

    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        this.reservations = List.copyOf(reservations);
    }

    public boolean isOccupied(ReservationTime time) {
        return reservations.stream()
                .anyMatch(r -> r.isAtTime(time));
    }

    public Reservation findByTime(ReservationTime time) {
        return reservations.stream()
                .filter(r -> r.isAtTime(time))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleViolationException(NOT_RESERVED_SLOT));
    }

    public Reservations excluding(Long reservationId) {
        return new Reservations(reservations.stream()
                .filter(r -> !reservationId.equals(r.getId()))
                .toList());
    }
}
