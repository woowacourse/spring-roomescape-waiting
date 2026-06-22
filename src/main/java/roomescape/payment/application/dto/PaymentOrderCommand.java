package roomescape.payment.application.dto;

import java.time.LocalDate;

public record PaymentOrderCommand(
        String name,
        LocalDate date,
        Long themeId,
        Long timeId
) {
}
