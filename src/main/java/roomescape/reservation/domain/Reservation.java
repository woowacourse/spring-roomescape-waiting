package roomescape.reservation.domain;

import roomescape.theme.domain.Theme;

import java.time.LocalDate;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private ReservationStatus status;

    private Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme,
        ReservationStatus status) {
        validateName(name);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 예약자 이름은 공백일 수 없습니다.");
        }
    }

    public static Reservation of(Long id, String name, LocalDate date, ReservationTime time, Theme theme, ReservationStatus status) {
        return new Reservation(id, name, date, time, theme, status);
    }

    public void promote(ReservationStatus status) {
        this.status = this.status.transitionTo(status);
    }

    public boolean isReserved() {
        return this.status == ReservationStatus.RESERVED;
    }

    public boolean isCanceled() {
        return this.status == ReservationStatus.CANCELED;
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

    public ReservationStatus getStatus() {
        return status;
    }
}
