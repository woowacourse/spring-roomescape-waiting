package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import roomescape.domain.exception.PastReservationException;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "reservation_id"})
})
public class ReservationWaiting {

    private static final long FIRST_PRIORITY = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Reservation reservation;

    @Column(nullable = false)
    private long priority;

    protected ReservationWaiting() {
    }

    public ReservationWaiting(Member member, Reservation reservation, long priority) {
        this(null, member, reservation, priority);
    }

    public ReservationWaiting(Long id, Member member, Reservation reservation, long priority) {
        validate(priority);
        this.id = id;
        this.member = member;
        this.reservation = reservation;
        this.priority = priority;
    }

    public static ReservationWaiting create(Member member, Reservation reservation, long priority) {
        ReservationWaiting newInstance = new ReservationWaiting(member, reservation, priority);
        validatePast(newInstance);
        validateWaitingMember(member, reservation);
        return newInstance;
    }

    private static void validatePast(ReservationWaiting reservationWaiting) {
        if (reservationWaiting.isPast()) {
            throw new PastReservationException();
        }
    }

    private static void validateWaitingMember(final Member member, final Reservation reservation) {
        if (Objects.equals(member.getId(), reservation.getMember().getId())) {
            throw new IllegalStateException("예약자는 예약대기를 할 수 없습니다.");
        }
    }

    private void validate(long priority) {
        if (priority < FIRST_PRIORITY) {
            throw new IllegalArgumentException("우선 순위는 1 이상의 숫자입니다.");
        }
    }

    private boolean isPast() {
        return reservation.isPast();
    }

    public void approveWaitingMember() {
        reservation.changeMember(member);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Long getPriority() {
        return priority;
    }

    public Reservation getReservation() {
        return reservation;
    }
}
