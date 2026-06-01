package roomescape.service.dto;

import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;

public record UserReservation(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        ReservationStatus status,
        Long rank
) {

    public static UserReservation from(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return new UserReservation(id, name, date, time, theme, ReservationStatus.RESERVED, 0L);
    }

    public static UserReservation from(Long id, String name, LocalDate date, ReservationTime time, Theme theme, long entryRank) {
        return new UserReservation(id, name, date, time, theme, ReservationStatus.WAITING, entryRank);
    }

    public static UserReservation waiting(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return new UserReservation(id, name, date, time, theme, ReservationStatus.WAITING, null);
    }
}