package roomescape.domain;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
public class Reservation {

    private final Long id;
    private final String username;
    private final LocalDate reservationDate;
    private final ReservationTime reservationTime;
    private final Theme reservationTheme;

    public static Reservation create(long id, String username, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(id, username, date, time, theme);
    }

    public String username() {
        return username;
    }

    public LocalDate reservationDate() {
        return reservationDate;
    }

    public ReservationTime reservationTime() {
        return reservationTime;
    }

    public Theme reservationTheme() {
        return reservationTheme;
    }

    public long id() {
        return id;
    }
}
