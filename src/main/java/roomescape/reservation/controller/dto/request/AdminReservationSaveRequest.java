package roomescape.reservation.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminReservationSaveRequest(
        @NotNull
        long memberId,

        @NotNull
        long themeId,

        @NotNull
        LocalDate date,

        @NotNull
        long timeId
) {

    public ReservationSaveRequest toReservationSaveRequest() {
        return new ReservationSaveRequest(themeId, date, timeId);
    }
}
