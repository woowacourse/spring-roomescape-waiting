package roomescape.slot.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import roomescape.slot.Slot;

import java.time.LocalDate;

public record SlotSaveRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {
    public Slot toDomain() {
        return new Slot(
                null,
                date,
                timeId,
                themeId
        );
    }
}
