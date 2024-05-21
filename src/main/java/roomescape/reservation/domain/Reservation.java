package roomescape.reservation.domain;

import jakarta.persistence.*;
import roomescape.exception.BadRequestException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @JoinColumn(name = "time_id")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private ReservationTime time;
    @JoinColumn(name = "theme_id")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Theme theme;

    protected Reservation() {
    }

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme) {
        validateNullField(date, time, theme);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validateNullField(LocalDate date, ReservationTime time, Theme theme) {
        if (date == null || time == null || theme == null) {
            throw new IllegalArgumentException("예약 필드에는 빈 값이 들어올 수 없습니다.");
        }
    }

    public Reservation(LocalDate date, ReservationTime reservationTime, Theme theme) {
        this(null, date, reservationTime, theme);
    }

    public void validateIsBeforeNow() {
        LocalDateTime dateTime = LocalDateTime.of(date, time.getStartAt());
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("이미 지난 날짜는 예약할 수 없습니다.");
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reservation that = (Reservation) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
