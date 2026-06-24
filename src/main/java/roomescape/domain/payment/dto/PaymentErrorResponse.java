package roomescape.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentErrorResponse(
    String code,
    String message
) {

}
