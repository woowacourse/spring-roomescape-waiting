package roomescape.service.dto;

import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import roomescape.service.exception.BusinessException;
import roomescape.service.exception.ErrorCode;
import roomescape.domain.ReservationStatus;

public record UserReservation(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        ReservationStatus status,
        Long rank
) {

    public static UserReservation reserved(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return new UserReservation(id, name, date, time, theme, ReservationStatus.RESERVED, 0L);
    }

    public static UserReservation waiting(Long id, String name, LocalDate date, ReservationTime time, Theme theme, long entryRank) {
        if (entryRank <= 0) {
            throw new BusinessException(ErrorCode.INVALID_WAITING_RANK);
        }
        return new UserReservation(id, name, date, time, theme, ReservationStatus.WAITING, entryRank);
    }
}
