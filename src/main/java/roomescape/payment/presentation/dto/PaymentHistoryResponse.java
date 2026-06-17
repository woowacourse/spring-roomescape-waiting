package roomescape.payment.presentation.dto;

import java.time.format.DateTimeFormatter;
import lombok.Builder;
import roomescape.payment.application.dto.OrderInfo;

@Builder
public record PaymentHistoryResponse(
        String at,
        boolean success,
        String status,
        String orderId,
        Long amount,
        String detail
) {
    public static PaymentHistoryResponse from(OrderInfo info) {
        return PaymentHistoryResponse.builder()
                .at(info.createdAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .success("COMPLETED".equals(info.status().name()))
                .status(info.status().name())
                .orderId(info.orderId())
                .amount(info.amount())
                .detail(info.themeName() + " 테마 예약")
                .build();
    }
}
