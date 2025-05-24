package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne
    @JoinColumn(name = "schedule_id")
    private ReservationSchedule schedule;

    protected Reservation() {
    }

    public Reservation(
            final Long id,
            final Member member,
            final ReservationSchedule schedule
    ) {
        this.id = id;
        this.member = Objects.requireNonNull(member);
        this.schedule = Objects.requireNonNull(schedule);
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

    public ReservationTime getReservationTime() {
        return schedule.getReservationTime();
    }

    public LocalTime getStartAt() {
        return schedule.getStartAt();
    }

    public Theme getTheme() {
        return schedule.getTheme();
    }

    public ReservationSchedule getSchedule() {
        return schedule;
    }
}
