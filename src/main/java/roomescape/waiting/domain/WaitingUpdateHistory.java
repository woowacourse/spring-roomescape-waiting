package roomescape.waiting.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import roomescape.reservation.domain.Reservation;

@Entity
public class WaitingUpdateHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Reservation reservation;

    private LocalDateTime expireTime;

    public WaitingUpdateHistory(Long id, Reservation reservation, LocalDateTime expireTime) {
        this.id = id;
        this.reservation = reservation;
        this.expireTime = expireTime;
    }

    public WaitingUpdateHistory(Reservation reservation, LocalDateTime expireTime) {
        this(null, reservation, expireTime);
    }

    public WaitingUpdateHistory() {
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }
}
