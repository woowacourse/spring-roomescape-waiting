package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "waiting")
public class ReservationStatus {

    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "reservation_id")
    @MapsId
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

    protected ReservationStatus() {
    }

    public ReservationStatus(Reservation reservation, BookStatus booked) {
        this.reservation = reservation;
        this.status = booked;
    }

    public void book() {
        this.status = BookStatus.BOOKED;
    }

    public boolean isOwnedBy(long memberId) {
        return reservation.isNotOwnedBy(memberId);
    }

    public boolean isBooked() {
        return status == BookStatus.BOOKED;
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }
}
