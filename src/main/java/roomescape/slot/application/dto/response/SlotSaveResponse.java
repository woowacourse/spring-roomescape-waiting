package roomescape.slot.application.dto.response;

import java.time.LocalDate;
import roomescape.slot.domain.Slot;

public record SlotSaveResponse(
        long id,
        LocalDate date,
        long time_id,
        long theme_id,
        int price
) {
    public SlotSaveResponse(long id, LocalDate date, long time_id, long theme_id) {
        this(id, date, time_id, theme_id, 0);
    }

    public static SlotSaveResponse from(Slot slot) {
        return new SlotSaveResponse(
                slot.getId(),
                slot.getDate(),
                slot.getTimeId(),
                slot.getThemeId(),
                slot.getPrice()
        );
    }
}
