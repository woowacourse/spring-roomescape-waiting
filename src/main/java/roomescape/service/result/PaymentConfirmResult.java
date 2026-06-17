package roomescape.service.result;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.client.dto.TossPaymentResponse;

public record PaymentConfirmResult(
        TossPaymentResponse response,
        String themeName,
        String themeThumbnailUrl,
        LocalDate reservationDate,
        LocalTime reservationTime
) {
}
