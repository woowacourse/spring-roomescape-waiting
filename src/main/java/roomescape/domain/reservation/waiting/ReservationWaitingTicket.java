package roomescape.domain.reservation.waiting;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import roomescape.domain.reservation.Reservation;

@Entity
public class ReservationWaitingTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Reservation reservation;

    private LocalDateTime createdAt;

    public ReservationWaitingTicket(Reservation reservation) {
        this.reservation = reservation;
        this.createdAt = LocalDateTime.now();
    }

    protected ReservationWaitingTicket() {
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
