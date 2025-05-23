package roomescape.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.waiting.service.dto.WaitingInfo;

public record MyReservationResponse(
        long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public MyReservationResponse(final ReservationInfo reservationInfo) {
        this(reservationInfo.id(),
                reservationInfo.theme().name(),
                reservationInfo.date(),
                reservationInfo.time().startAt(),
                "예약"
        );
    }

    public MyReservationResponse(final WaitingInfo waitingInfo) {
        this(waitingInfo.id(),
                waitingInfo.theme().name(),
                waitingInfo.date(),
                waitingInfo.time().startAt(),
                "%d번째 예약대기".formatted(waitingInfo.order())
        );
    }
}
