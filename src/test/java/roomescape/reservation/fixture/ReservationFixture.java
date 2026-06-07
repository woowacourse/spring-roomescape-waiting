package roomescape.reservation.fixture;

import roomescape.date.domain.ReservationDate;
import roomescape.reservation.controller.dto.request.ReservationSaveDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.service.dto.ReservationSaveCommand;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public class ReservationFixture {

    public static Reservation reservation(
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return Reservation.reserved(name, date, time, theme);
    }

    public static Reservation waitReservation(
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        Long waitingOrder
    ) {
        return Reservation.wait(name, date, time, theme, waitingOrder);
    }

    public static Reservation waitReservation(
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return waitReservation(name, date, time, theme, 1L);
    }

    public static Reservation canceledReservation(
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        Reservation reservation = Reservation.reserved(name, date, time, theme);
        reservation.updateStatus(ReservationStatus.CANCELED);
        return reservation;
    }

    public static ReservationSaveCommand toCommand(
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return new ReservationSaveCommand(date.getId(), time.getId(), theme.getId());
    }

    public static ReservationSaveCommand toCommand(
        ReservationDate date,
        Long timeId,
        Theme theme
    ) {
        return new ReservationSaveCommand(date.getId(), timeId, theme.getId());
    }

    public static ReservationSaveDto toCommand(
        Long dateId,
        ReservationTime time,
        Theme theme
    ) {
        return new ReservationSaveDto(dateId, time.getId(), theme.getId());
    }

    public static ReservationSaveCommand toCommand(
        ReservationDate date,
        ReservationTime time,
        Long themeId
    ) {
        return new ReservationSaveCommand(date.getId(), time.getId(), themeId);
    }

}
