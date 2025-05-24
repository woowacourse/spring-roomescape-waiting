package roomescape.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.domain.member.Member;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Member member;

    @Embedded
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

    public ReservationTime getReservationTime() {
        return schedule.getReservationTime();
    }

    public Theme getTheme() {
        return schedule.getTheme();
    }

    public LocalDate getDate() {
        return schedule.getReservationDate().date();
    }

    public LocalTime getStartAt() {
        return schedule.getReservationTime().getStartAt();
    }
}
