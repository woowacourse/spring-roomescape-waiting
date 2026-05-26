package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.Objects;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class Reservation {
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        Objects.requireNonNull(name, "예약자명은 필수값 입니다.");
        Objects.requireNonNull(date, "예약 날짜는 필수값 입니다.");
        Objects.requireNonNull(time, "예약 시간은 필수값 입니다.");
        Objects.requireNonNull(theme, "테마는 필수값 입니다.");
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation createWithoutId(String name, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(null, name, date, time, theme);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Reservation reservation = (Reservation) object;
        if (id != null && reservation.id != null) {
            return Objects.equals(id, reservation.id);
        }
        return Objects.equals(name, reservation.name)
                && Objects.equals(date, reservation.date) && Objects.equals(time, reservation.time)
                && Objects.equals(theme, reservation.theme);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name, date, time, theme);
    }
}
