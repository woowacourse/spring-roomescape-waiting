package roomescape.reservation.controller.response;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.time.controller.response.ReservationTimeResponse;

public record MemberReservationResponse(

        Long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status,
        Long rank
) {

    public static MemberReservationResponse from(Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getReservationTime()),
                ThemeResponse.from(reservation.getTheme()),
                "예약",
                0L
        );
    }

    public static MemberReservationResponse from(WaitingWithRank waitingWithRank) {
        return new MemberReservationResponse(
                waitingWithRank.waiting().getId(),
                waitingWithRank.waiting().getReservation().getDate(),
                ReservationTimeResponse.from(waitingWithRank.waiting().getReservation().getReservationTime()),
                ThemeResponse.from(waitingWithRank.waiting().getReservation().getTheme()),
                "예약대기",
                waitingWithRank.rank() + 1L
        );
    }
}
