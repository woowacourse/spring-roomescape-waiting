package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TIME_ID")
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "THEME_ID")
    private Theme theme;

    public Reservation() {
    }

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme) {
        validate(date, time);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(LocalDate date, ReservationTime time, Theme theme) {
        this(null, date, time, theme);
    }

    public boolean isPast() {
        return LocalDateTime.of(this.date, this.time.getStartAt()).isBefore(LocalDateTime.now());
    }


    private void validate(LocalDate date, ReservationTime time) {
        if (date == null || time == null) {
            throw new BadRequestException(ErrorType.MISSING_REQUIRED_VALUE_ERROR);
        }
    }

    public Long getId() {
        return id;
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation)) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", date=" + date +
                ", time=" + time +
                ", theme=" + theme +
                '}';
    }
}
