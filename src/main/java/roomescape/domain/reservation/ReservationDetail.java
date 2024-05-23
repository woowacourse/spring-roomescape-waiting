package roomescape.domain.reservation;

import jakarta.persistence.*;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationDetail other)) return false;
        return Objects.equals(id, other.id);
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

    public Schedule getSchedule() {
        return schedule;
    }
}
