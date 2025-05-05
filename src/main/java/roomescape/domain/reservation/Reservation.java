package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.exception.reservation.InvalidReservationException;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        validate(name, date, time);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validate(String name, LocalDate date, ReservationTime time) {
        if (name == null || name.isBlank()) {
            throw new InvalidReservationException("이름은 공백일 수 없습니다");
        }
        if (date == null || time == null) {
            throw new InvalidReservationException("시간은 공백일 수 없습니다.");
        }
    }

    public boolean isBefore(LocalDateTime compareDateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, getStartAt());
        return reservationDateTime.isBefore(compareDateTime);
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

    public ReservationTime getReservationTime() {
        return time;
    }

    public LocalTime getStartAt() {
        return time.getTime();
    }

    public Theme getTheme() {
        return theme;
    }

    public String getThemeName() {
        return theme.getName();
    }
}
