package roomescape.service.result;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.PaymentResult;

public record PaymentConfirmResult(
        PaymentResult response,
        String themeName,
        String themeThumbnailUrl,
        LocalDate reservationDate,
        LocalTime reservationTime
) {
}
