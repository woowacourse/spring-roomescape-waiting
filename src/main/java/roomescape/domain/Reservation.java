package roomescape.domain;

import lombok.RequiredArgsConstructor;
import roomescape.exception.PastReservationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public boolean isPast(LocalDateTime now) {
        return reservationTime.isPast(reservationDate, now);
    }

    public boolean isOwnedBy(String name) {
        return this.username.equals(name);
    }

    public void validateCancelable(LocalDateTime now) {
        if (isPast(now)) {
            throw new PastReservationException("이미 시작된 예약은 취소할 수 없습니다.");
        }
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
