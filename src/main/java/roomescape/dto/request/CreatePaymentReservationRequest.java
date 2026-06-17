package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreatePaymentReservationRequest(
        @NotNull Long themeId,
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long storeId,
        @NotNull @Positive Long amount
) {
}
