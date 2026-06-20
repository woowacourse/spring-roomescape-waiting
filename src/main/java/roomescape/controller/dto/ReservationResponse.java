package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.payment.PaymentOrderStatus;
import roomescape.service.dto.ReservationResult;

public record ReservationResponse(
        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Long waitingOrder,
        String orderId,
        String paymentKey,
        Long amount,
        PaymentOrderStatus paymentStatus
) {
    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(
                result.id(),
                result.name(),
                result.date(),
                ReservationTimeResponse.from(result.time()),
                ThemeResponse.from(result.theme()),
                result.waitingOrder(),
                result.orderId(),
                result.paymentKey(),
                result.amount(),
                result.paymentStatus()
        );
    }
}
