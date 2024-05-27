package roomescape.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import roomescape.domain.member.Member;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @Embedded
    private Schedule schedule;

    protected Reservation() {
    }

    public Reservation(Long id, Member member, Schedule schedule) {
        this.id = id;
        this.member = member;
        this.schedule = schedule;
    }

    public Reservation(Member member, Schedule schedule) {
        this(null, member, schedule);
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

    public LocalDate getDate() {
        return schedule.getDate();
    }

    public ReservationTime getTime() {
        return schedule.getTime();
    }

    public Theme getTheme() {
        return schedule.getTheme();
    }

    public Schedule getSchedule() {
        return schedule;
    }
}
