package roomescape.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import roomescape.domain.Schedule;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Embedded
    private Schedule schedule;

    protected Reservation() {
    }

    public Reservation(final Member member, final Schedule schedule) {
        this.member = member;
        this.schedule = schedule;
    }

    public boolean isSameTime(ReservationTime reservationTime) {
        return schedule.isSameTime(reservationTime);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setMember(final Member member) {
        this.member = member;
    }
}
