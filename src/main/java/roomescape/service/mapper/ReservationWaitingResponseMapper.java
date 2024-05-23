package roomescape.service.mapper;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.ReservationTimeResponse;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.dto.ThemeResponse;

public class ReservationWaitingResponseMapper {
    public static ReservationWaitingResponse toResponseWithoutPriority(ReservationWaiting reservationWaiting) {
        Reservation reservation = reservationWaiting.getReservation();

        ReservationTime reservationTime = reservation.getReservationTime();
        ReservationTimeResponse timeResponse = ReservationTimeResponseMapper.toResponse(reservationTime);

        Theme theme = reservation.getTheme();
        ThemeResponse themeResponse = ThemeResponseMapper.toResponse(theme);

        String waitingMemberName = reservationWaiting.getWaitingMember().getName();

        return new ReservationWaitingResponse(reservationWaiting.getId(), waitingMemberName,
                reservation.getDate(), timeResponse, themeResponse, null);
    }

    public static ReservationWaitingResponse toResponse(ReservationWaiting target, int priority) {
        Reservation reservation = target.getReservation();

        ReservationTime reservationTime = reservation.getReservationTime();
        ReservationTimeResponse timeResponse = ReservationTimeResponseMapper.toResponse(reservationTime);

        Theme theme = reservation.getTheme();
        ThemeResponse themeResponse = ThemeResponseMapper.toResponse(theme);

        String waitingMemberName = target.getWaitingMember().getName();

        return new ReservationWaitingResponse(target.getId(), waitingMemberName,
                reservation.getDate(), timeResponse, themeResponse, priority);
    }
}
