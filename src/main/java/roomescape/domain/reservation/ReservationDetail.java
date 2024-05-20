package roomescape.domain.reservation;

import jakarta.persistence.*;
import roomescape.domain.member.Member;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class ReservationDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "date.value", column = @Column(name = "DATE"))
    private Schedule schedule;

    @ManyToOne
    private Theme theme;

    protected ReservationDetail() {
    }

    public ReservationDetail(Schedule schedule, Theme theme) {
        this.schedule = schedule;
        this.theme = theme;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return schedule.getDate();
    }

    public LocalTime getTime() {
        return schedule.getTime();
    }

    public ReservationTime getReservationTime() {
        return schedule.getReservationTime();
    }

    public Theme getTheme() {
        return theme;
    }
}
