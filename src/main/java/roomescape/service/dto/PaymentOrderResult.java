package roomescape.service.dto;

public record PaymentOrderResult(
        String orderId,
        Long amount,
        String orderName
) {
}
