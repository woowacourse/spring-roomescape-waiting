package roomescape.domain;

import java.time.LocalDate;

public class ReservationOrder {
    private Reservation reservation;
    private Long order;

    public ReservationOrder(Reservation reservation, Long order) {
        this.reservation = reservation;
        this.order = order;
    }

    public Long getId() {
        return reservation.getId();
    }

    public String getName() {
        return reservation.getName();
    }

    public LocalDate getDate() {
        return reservation.getDate();
    }

    public ReservationTime getTime() {
        return reservation.getTime();
    }

    public Theme getTheme() {
        return reservation.getTheme();
    }

    public ReservationStatus getStatus() {
        return reservation.getStatus();
    }

    public Long getOrder() {
        return order;
    }
}
