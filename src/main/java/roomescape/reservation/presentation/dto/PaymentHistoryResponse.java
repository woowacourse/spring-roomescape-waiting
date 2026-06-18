package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.reservation.application.dto.PaymentHistoryResult;
import roomescape.reservationtime.presentation.dto.ReservationTimeResponse;
import roomescape.theme.presentation.dto.ThemeResponse;

public record PaymentHistoryResponse(
        Long reservationId,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        String reservationStatus,
        String orderId,
        Long amount,
        String paymentKey,
        String paymentStatus
) {

    public static PaymentHistoryResponse from(PaymentHistoryResult result) {
        return new PaymentHistoryResponse(
                result.reservationId(),
                result.username(),
                result.date(),
                ThemeResponse.from(result.theme()),
                ReservationTimeResponse.from(result.time()),
                result.reservationStatus(),
                result.orderId(),
                result.amount(),
                result.paymentKey(),
                result.paymentStatus()
        );
    }
}
