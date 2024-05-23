package roomescape.service.reservation.dto;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.WaitingWithRank;

public record MyReservationResponse(Long id,
                                    LocalDate date,
                                    ReservationTimeResponse time,
                                    ThemeResponse theme,
                                    String status) {

    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                "예약"
        );
    }

    public static MyReservationResponse from(WaitingWithRank waiting) {
        return new MyReservationResponse(
                waiting.getWaiting().getId(),
                waiting.getWaiting().getDate(),
                ReservationTimeResponse.from(waiting.getWaiting().getTime()),
                ThemeResponse.from(waiting.getWaiting().getTheme()),
                String.format("%d번째 예약대기", waiting.getRank() + 1)
        );
    }
}
