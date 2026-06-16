package roomescape.dto.payment;

import java.time.LocalDate;

public record PaymentPrepareRequest(
        String orderId,
        Long amount,
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}