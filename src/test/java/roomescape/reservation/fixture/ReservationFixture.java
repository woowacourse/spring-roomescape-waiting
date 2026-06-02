package roomescape.reservation.fixture;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import roomescape.date.domain.ReservationDate;
import roomescape.reservation.domain.Reservation;
import roomescape.slot.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.controller.dto.request.ReservationSaveDto;
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
        return Reservation.reserve(name, ReservationSlot.of(date, time, theme), LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    public static Reservation reservation(String name, ReservationSlot slot) {
        return Reservation.reserve(name, slot, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    public static Reservation reservation(String name, ReservationSlot slot, LocalDateTime reservedAt) {
        return Reservation.reserve(name, slot, reservedAt.truncatedTo(ChronoUnit.MICROS));
    }

    public static Reservation reservation(
            String name,
            ReservationDate date,
            ReservationTime time,
            Theme theme,
            LocalDateTime reservedAt
    ) {
        return Reservation.reserve(name, ReservationSlot.of(date, time, theme), reservedAt.truncatedTo(ChronoUnit.MICROS));
    }

    public static Reservation waitReservation(String name, ReservationSlot slot) {
        return Reservation.wait(name, slot, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    public static Reservation waitReservation(
            String name,
            ReservationDate date,
            ReservationTime time,
            Theme theme
    ) {
        return Reservation.wait(name, ReservationSlot.of(date, time, theme), LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    public static Reservation canceledReservation(
            String name,
            ReservationDate date,
            ReservationTime time,
            Theme theme
    ) {
        Reservation reservation = Reservation.reserve(name, ReservationSlot.of(date, time, theme), LocalDateTime.now());
        reservation.updateStatus(ReservationStatus.CANCELED);
        return reservation;
    }

    public static Reservation canceledReservation(String name, ReservationSlot slot) {
        Reservation reservation = Reservation.reserve(name, slot, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
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
