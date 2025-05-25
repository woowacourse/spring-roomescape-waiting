package roomescape.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import roomescape.domain.member.Member;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startedAt;

    @Embedded
    private ReservationSlot reservationSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    protected Waiting() {
    }

    public Waiting(Long id, LocalDateTime startedAt, ReservationSlot reservationSlot, Member member) {
        this.id = id;
        this.startedAt = startedAt;
        this.reservationSlot = reservationSlot;
        this.member = member;
    }

    public static Waiting create(LocalDateTime startedAt, ReservationSlot reservationSlot, Member member) {
        reservationSlot.validateReservable(startedAt);
        return new Waiting(null, startedAt, reservationSlot, member);
    }

    public Reservation toReservation() {
        return Reservation.create(member, reservationSlot);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public ReservationSlot getThemeSchedule() {
        return reservationSlot;
    }

    public Member getMember() {
        return member;
    }
}
