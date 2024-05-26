package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import roomescape.exception.BadRequestException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"theme_id", "time_id", "date"})
})
public class ReservationDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private Time time;
    @Column(nullable = false, name = "date")
    private LocalDate date;

    public ReservationDetail() {
    }

    public ReservationDetail(Theme theme, Time time, LocalDate date) {
        this(null, theme, time, date);
    }

    public ReservationDetail(Long id, Theme theme, Time time, LocalDate date) {
        validate(date, time, theme);
        this.id = id;
        this.theme = theme;
        this.time = time;
        this.date = date;
    }

    private void validate(LocalDate date, Time time, Theme theme) {
        if (date == null || time == null || theme == null) {
            throw new BadRequestException("예약 정보가 부족합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public boolean isReservedAtPeriod(LocalDate start, LocalDate end) {
        return date.isAfter(start) && date.isBefore(end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationDetail that)) return false;

        if (id == null || that.id == null) {
            return Objects.equals(date, that.date) && Objects.equals(time, that.time)
                   && Objects.equals(theme, that.theme);
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        if (id == null) return Objects.hash(date, time, theme);
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ReservationDetail{" +
               "id=" + id +
               ", theme=" + theme +
               ", time=" + time +
               ", date=" + date +
               '}';
    }
}
