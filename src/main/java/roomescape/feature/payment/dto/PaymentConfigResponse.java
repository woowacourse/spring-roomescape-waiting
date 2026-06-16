package roomescape.feature.payment.dto;

public record PaymentConfigResponse(
        String clientKey,
        Long amount
) {
}
