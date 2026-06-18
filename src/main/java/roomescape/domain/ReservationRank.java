package roomescape.domain;

import java.time.LocalDate;

public class ReservationRank {
    private Reservation reservation;
    private Long rank;

    public ReservationRank(Reservation reservation, Long rank) {
        this.reservation = reservation;
        this.rank = rank;
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

    public Long getRank() {
        return rank;
    }

    public String getOrderId() {
        return reservation.getOrderId();
    }

    public String getPaymentKey() {
        return reservation.getPaymentKey();
    }

    public Long getAmount() {
        return reservation.getAmount();
    }
}
