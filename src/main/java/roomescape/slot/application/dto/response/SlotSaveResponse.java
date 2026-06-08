package roomescape.slot.application.dto.response;

import java.time.LocalDate;
import roomescape.slot.domain.Slot;

public record SlotSaveResponse(
        long id,
        LocalDate date,
        long time_id,
        long theme_id
) {
    public static SlotSaveResponse from(Slot slot) {
        return new SlotSaveResponse(
                slot.getId(),
                slot.getDate(),
                slot.getTimeId(),
                slot.getThemeId()
        );
    }
}
