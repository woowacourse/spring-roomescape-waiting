package roomescape.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentConfirmResponse(
    String paymentKey,
    String orderId,
    Long totalAmount,
    String status
) {

}
