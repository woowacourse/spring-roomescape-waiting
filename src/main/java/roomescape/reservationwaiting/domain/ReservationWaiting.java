package roomescape.reservationwaiting.domain;

import lombok.Builder;
import roomescape.reservation.domain.Reservation;

public class ReservationWaiting {
    private final Long id;
    private final String name;
    private final Reservation reservation;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private ReservationWaiting(Long id, String name, Reservation reservation) {
        this.id = id;
        this.name = name;
        this.reservation = reservation;
    }

    public static ReservationWaiting restore(Long id, String name, Reservation reservation) {
        return ReservationWaiting.builder().
                id(id).name(name).reservation(reservation)
                .build();
    }

    public static ReservationWaiting restoreWithTurn(Long id, String name, Reservation reservation, Long turn) {
        return ReservationWaiting.builder().
                id(id).name(name).reservation(reservation)
                .build();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Reservation getReservation() {
        return reservation;
    }
}
