package roomescape.waiting.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import roomescape.waiting.Waiting;

import java.time.LocalDate;

public record WaitingRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {
    public Waiting toDomain(long memberId, long slotId) {
        return new Waiting(
                null,
                memberId,
                slotId
        );
    }
}
