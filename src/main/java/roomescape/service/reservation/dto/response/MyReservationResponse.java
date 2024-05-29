package roomescape.service.reservation.dto.response;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.WaitingWithRank;

public record MyReservationResponse(Long id,
                                    LocalDate date,
                                    ReservationTimeResponse time,
                                    ThemeResponse theme,
                                    long waitingCount) {

    public static final int DEFAULT_WAITING_COUNT = 1;

    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                0
        );
    }

    public static MyReservationResponse from(WaitingWithRank waiting) {
        return new MyReservationResponse(
                waiting.getWaiting().getId(),
                waiting.getWaiting().getDate(),
                ReservationTimeResponse.from(waiting.getWaiting().getTime()),
                ThemeResponse.from(waiting.getWaiting().getTheme()),
                waiting.getRank() + DEFAULT_WAITING_COUNT
        );
    }
}
