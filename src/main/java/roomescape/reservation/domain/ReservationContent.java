package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.exception.BadRequestException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date", "time_id", "theme_id"})
})
public class ReservationContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "time_id")
    private Time time;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    protected ReservationContent() {
    }

    public ReservationContent(LocalDate date, Time time, Theme theme) {
        validate(date, time, theme);
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validate(LocalDate date, Time time, Theme theme) {
        if (date == null || time == null || theme == null) {
            throw new BadRequestException("예약 세부 정보가 부족합니다.");
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

    public Theme getTheme() {
        return theme;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReservationContent that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(date, that.date)
                && Objects.equals(time, that.time) && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, time, theme);
    }

    @Override
    public String toString() {
        return "ReservationContent{" +
                "id=" + id +
                ", date=" + date +
                ", time=" + time +
                ", theme=" + theme +
                '}';
    }
}
