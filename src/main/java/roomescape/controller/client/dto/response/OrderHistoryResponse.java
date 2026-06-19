package roomescape.controller.client.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.service.result.OrderHistoryResult;

public record OrderHistoryResponse(
        Long reservationId,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String themeName,
        String entryStatus,
        String orderId,
        String paymentKey,
        Long amount,
        String paymentStatus
) {

    public static OrderHistoryResponse from(OrderHistoryResult result) {
        return new OrderHistoryResponse(
                result.reservationId(),
                result.date(),
                result.time(),
                result.themeName(),
                result.entryStatus(),
                result.orderId(),
                result.paymentKey(),
                result.amount(),
                result.paymentStatus()
        );
    }
}
