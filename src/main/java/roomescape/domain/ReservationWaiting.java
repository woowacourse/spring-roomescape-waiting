package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ReservationWaiting {

    private static final long FIRST_PRIORITY = 0L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

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

    private void validate(long priority) {
        if (priority < FIRST_PRIORITY) {
            throw new IllegalArgumentException("우선 순위는 1 이상의 숫자입니다.");
        }
    }

    public boolean isPast() {
        return reservation.isPast();
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
