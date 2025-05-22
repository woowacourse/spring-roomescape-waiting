package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Waiting extends Booking {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime waitingStartedAt;

    protected Waiting() {
    }

    private Waiting(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        super(id, member, date, time, theme);
    }

    public static Waiting create(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return new Waiting(null, member, date, time, theme);
    }

    public Reservation toReservation() {
        return Reservation.create(getMember(), getDate(), getTime(), getTheme());
    }

    public boolean sameWaiterWith(Long memberId) {
        return getMember().getId().equals(memberId);
    }
}
