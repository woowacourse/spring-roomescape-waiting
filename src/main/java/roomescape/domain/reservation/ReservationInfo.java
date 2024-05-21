package roomescape.domain.reservation;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "reservation_info", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date", "time_id", "theme_id"})
})
public class ReservationInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationDate date;

    @ManyToOne
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    protected ReservationInfo() {
    }

    public ReservationInfo(final ReservationDate date, final ReservationTime time, final Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationInfo from(final String date, final ReservationTime time, final Theme theme) {
        return new ReservationInfo(ReservationDate.from(date), time, theme);
    }

    public Long getId() {
        return id;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }


    public String getLocalDateTimeFormat() {
        return parseLocalDateTime().toString();
    }

    public boolean isBefore(final LocalDate localDate, final LocalTime localTime) {
        return parseLocalDateTime().isBefore(LocalDateTime.of(localDate, localTime));
    }

    public LocalDateTime parseLocalDateTime() {
        return LocalDateTime.of(date.value(), this.time.getStartAt());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReservationInfo that = (ReservationInfo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", value=" + date +
                ", time=" + time +
                ", theme=" + theme +
                '}';
    }
}
