package roomescape.service.fixture;

import static roomescape.model.ReservationStatus.ACCEPT;
import static roomescape.model.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.time.LocalDateTime;

import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationStatus;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

public class TestReservationFactory {

    public static Reservation createReservation(Long id, LocalDate date,
                                                ReservationStatus status,
                                                LocalDateTime createdAt,
                                                ReservationTime time,
                                                Theme theme,
                                                Member member) {
        return new Reservation(id, date, status, createdAt, time, theme, member);
    }

    public static Reservation createReservation(Long id,
                                                LocalDate date,
                                                ReservationStatus status,
                                                ReservationTime time,
                                                Theme theme,
                                                Member member) {
        return createReservation(id, date, status, LocalDateTime.now(), time, theme, member);
    }

    public static Reservation createAcceptReservationAtNow(Long id, ReservationTime time, Theme theme, Member member) {
        return createReservation(id, LocalDate.now(), ACCEPT, LocalDateTime.now(), time, theme, member);
    }

    public static Reservation createAcceptReservation(Long id,
                                                      LocalDate date,
                                                      ReservationTime time,
                                                      Theme theme,
                                                      Member member) {
        return createReservation(id, date, ACCEPT, time, theme, member);
    }

    public static Reservation createWaiting(Long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        return createReservation(id, date, WAITING, time, theme, member);
    }
}
