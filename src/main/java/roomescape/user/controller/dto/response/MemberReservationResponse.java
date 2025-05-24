package roomescape.user.controller.dto.response;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.waiting.domain.WaitingWithRank;

public record MemberReservationResponse(
        Long id,
        LocalDate date,
        String name,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String reservationStatus
) {

    private static final String STATUS = "%d 번째 예약";

    public static MemberReservationResponse fromReservation(Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getMember().getName(),
                ReservationTimeResponse.from(reservation.getReservationTime()),
                ThemeResponse.from(reservation.getTheme()),
                "예약"
        );
    }

    public static MemberReservationResponse fromWaitingWithRank(WaitingWithRank waitingWithRank) {
        return new MemberReservationResponse(
                waitingWithRank.getWaiting().getId(),
                waitingWithRank.getWaiting().getDate(),
                waitingWithRank.getWaiting().getMember().getName(),
                ReservationTimeResponse.from(waitingWithRank.getWaiting().getReservationTime()),
                ThemeResponse.from(waitingWithRank.getWaiting().getTheme()),
                String.format(STATUS, waitingWithRank.getRank() + 1)
        );
    }
}
