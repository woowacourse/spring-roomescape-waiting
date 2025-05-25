package roomescape.user.controller.dto.response;

import static roomescape.global.ReservationStatus.RESERVATION;
import static roomescape.global.ReservationStatus.WAITING;

import java.time.LocalDate;
import roomescape.global.ReservationStatus;
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
        ReservationStatus reservationStatus,
        Long rank
) {

    public static final long DEFAULT_RESERVATION_RANK = 0L;

    public static MemberReservationResponse fromReservation(Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getMember().getName(),
                ReservationTimeResponse.from(reservation.getReservationTime()),
                ThemeResponse.from(reservation.getTheme()),
                RESERVATION,
                DEFAULT_RESERVATION_RANK
        );
    }

    public static MemberReservationResponse fromWaitingWithRank(WaitingWithRank waitingWithRank) {
        return new MemberReservationResponse(
                waitingWithRank.getWaiting().getId(),
                waitingWithRank.getWaiting().getDate(),
                waitingWithRank.getWaiting().getMember().getName(),
                ReservationTimeResponse.from(waitingWithRank.getWaiting().getReservationTime()),
                ThemeResponse.from(waitingWithRank.getWaiting().getTheme()),
                WAITING,
                waitingWithRank.getRank()
        );
    }
}
