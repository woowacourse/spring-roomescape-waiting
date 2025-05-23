package roomescape.schedule.domain;

import jakarta.persistence.*;
import roomescape.exception.DomainValidationException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "schedule")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    protected Schedule() {
    }

    public Schedule(Long id, LocalDate date, ReservationTime time, Theme theme) {
        validate(date, time, theme);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validate(LocalDate date, ReservationTime time, Theme theme) {
        if (date == null || time == null || theme == null) {
            throw new DomainValidationException("예약 정보가 비어있습니다.");
        }
    }

    public LocalDateTime getDateTime() {
        return LocalDateTime.of(date, time.getStartAt());
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Schedule schedule)) return false;
        return Objects.equals(id, schedule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
