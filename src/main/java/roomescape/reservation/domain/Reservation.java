package roomescape.reservation.domain;

import roomescape.theme.domain.Theme;

import java.time.LocalDate;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        validateName(name);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 예약자 이름은 공백일 수 없습니다.");
        }
    }

    public static Reservation of(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(id, name, date, time, theme);
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
}
