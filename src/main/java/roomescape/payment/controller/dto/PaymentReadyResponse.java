package roomescape.payment.controller.dto;

import roomescape.payment.config.PaymentProperties;
import roomescape.payment.service.PaymentReadyOrder;

public record PaymentReadyResponse(
        String clientKey,
        String orderId,
        long amount,
        String orderName,
        String customerName
) {

    public static PaymentReadyResponse from(PaymentReadyOrder paymentReadyOrder, PaymentProperties paymentProperties) {
        return new PaymentReadyResponse(
                paymentProperties.toss().clientKey(),
                paymentReadyOrder.orderId(),
                paymentReadyOrder.amount(),
                paymentReadyOrder.orderName(),
                paymentReadyOrder.customerName()
        );
    }
}
