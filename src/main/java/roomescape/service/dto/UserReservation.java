package roomescape.service.dto;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import roomescape.domain.Waiting;
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

    public static UserReservation reserved(Reservation reservation) {
        return new UserReservation(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                ReservationStatus.RESERVED,
                null
        );
    }

    public static UserReservation waiting(Waiting waiting, long entryRank) {
        if (entryRank <= 0) {
            throw new BusinessException(ErrorCode.INVALID_WAITING_RANK);
        }
        return new UserReservation(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme(),
                ReservationStatus.WAITING,
                entryRank
        );
    }
}
