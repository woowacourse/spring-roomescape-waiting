package roomescape.payment.controller.dto;

import roomescape.payment.domain.PaymentOrderDetails;

import java.util.List;

public record PaymentOrdersResponse(List<PaymentOrderResponse> orders) {

    public static PaymentOrdersResponse from(List<PaymentOrderDetails> paymentOrders) {
        return new PaymentOrdersResponse(
                paymentOrders.stream()
                        .map(PaymentOrderResponse::from)
                        .toList()
        );
    }
}
