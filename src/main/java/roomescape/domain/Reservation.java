package roomescape.domain;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Reservation {

    private final Long id;
    private final String name;
    private final Theme theme;
    private LocalDate date;
    private ReservationTime time;

    public Reservation(Long id, String name, LocalDate date, Theme theme, ReservationTime time) {
        validateReservation(name, date, theme, time);
        this.id = id;
        this.name = name;
        this.date = date;
        this.theme = theme;
        this.time = time;
    }

    public static Reservation createNew(String name, LocalDate date, Theme theme, ReservationTime time) {
        Reservation reservation = new Reservation(null, name, date, theme, time);
        validatePastDateTime(date, time);
        return reservation;
    }

    private static void validatePastDateTime(LocalDate date, ReservationTime time) {
        if (time.isPast(date)) {
            throw new IllegalArgumentException("이전 날짜로 예약 할 수 없습니다.");
        }
    }

    private static void validateReservation(String name, LocalDate date, Theme theme, ReservationTime time) {
        validateReservationName(name);
        validateTheme(theme);
        validateReservationDateTime(date, time);
    }

    private static void validateReservationName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 정보는 비어있을 수 없습니다.");
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("테마 정보는 비어있을 수 없습니다.");
        }
    }

    private static void validateReservationDateTime(LocalDate date, ReservationTime time) {
        if (date == null || time == null) {
            throw new IllegalArgumentException("예약 날짜 및 시간 정보는 비어있을 수 없습니다.");
        }
    }

    public void update(LocalDate date, ReservationTime time) {
        validateNotPast();
        validatePastDateTime(date, time);
        this.date = date;
        this.time = time;
    }

    public void validateNotPast() {
        if (this.time.isPast(this.date)) {
            throw new IllegalArgumentException("이미 지난 예약입니다.");
        }
    }

    public boolean isSameTime(LocalDate date, ReservationTime time) {
        return this.date.isEqual(date) && this.time.equals(time);
    }
}
