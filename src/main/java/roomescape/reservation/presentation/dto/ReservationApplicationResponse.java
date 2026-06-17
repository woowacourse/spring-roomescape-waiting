package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.reservation.application.dto.ReservationApplicationResult;
import roomescape.reservation.application.dto.ReservationApplicationResult.Status;
import roomescape.reservationtime.presentation.dto.ReservationTimeResponse;
import roomescape.theme.presentation.dto.ThemeResponse;

public record ReservationApplicationResponse(
        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        Status status,
        roomescape.payment.PaymentStatus paymentStatus,
        Long rank
) {
    public static ReservationApplicationResponse from(ReservationApplicationResult result) {
        return new ReservationApplicationResponse(
                result.id(),
                result.name(),
                result.date(),
                ThemeResponse.from(result.theme()),
                ReservationTimeResponse.from(result.time()),
                result.status(),
                result.paymentStatus(),
                result.rank()
        );
    }
}
