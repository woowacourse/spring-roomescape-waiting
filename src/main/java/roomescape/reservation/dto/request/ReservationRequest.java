package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ReservationRequest {

    public record ReservationCreateRequest(
            @NotNull LocalDate date,
            @NotNull Long timeId,
            @NotNull Long themeId
    ) {
    }

    public record ReservationAdminCreateRequest(
            @NotNull LocalDate date,
            @NotNull Long themeId,
            @NotNull Long timeId,
            @NotNull Long memberId
    ) {
    }

    public record ReservationReadFilteredRequest(
            @NotNull Long themeId,
            @NotNull Long memberId,
            @NotNull LocalDate dateFrom,
            @NotNull LocalDate dateTo
    ) {
    }
}
