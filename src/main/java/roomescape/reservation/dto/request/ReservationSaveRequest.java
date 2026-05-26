package roomescape.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import roomescape.reservation.Reservation;

import java.time.LocalDate;

public record ReservationSaveRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {
    public Reservation toDomain(long memberId, long scheduleId) {
        return new Reservation(
                null,
                memberId,
                scheduleId
        );
    }
}
