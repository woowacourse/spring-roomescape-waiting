package roomescape.waiting.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import roomescape.waiting.Waiting;

import java.time.LocalDate;

public record WaitingRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId,
        @Nullable Long reservationIdToCancel
) {
    public Waiting toDomain(long memberId, long scheduleId) {
        return new Waiting(
                null,
                memberId,
                scheduleId
        );
    }
}
