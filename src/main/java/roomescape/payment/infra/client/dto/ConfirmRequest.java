package roomescape.payment.infra.client.dto;

import lombok.Builder;

@Builder
public record ConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {

}
