package roomescape.service.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationWithWaiting;

public record MyReservationResponse(Long id,
                                    LocalDate date,
                                    LocalTime time,
                                    String theme,
                                    String status) {

    public static MyReservationResponse from(final ReservationWithWaiting reservationWithWaiting) {
        String status = "";
        if (reservationWithWaiting.getStatus() == ReservationStatus.RESERVATION) {
            status = "예약";
        }
        if (reservationWithWaiting.getStatus() == ReservationStatus.WAITING) {
            status = reservationWithWaiting.getRank() + "번째 예약대기";
        }

        return new MyReservationResponse(
                reservationWithWaiting.getId(),
                reservationWithWaiting.getDate(),
                reservationWithWaiting.getTime().getStartAt(),
                reservationWithWaiting.getTheme().getName(),
                status
        );
    }
}
