package roomescape.slot.application.dto.response;

import roomescape.slot.domain.Slot;

import java.time.LocalDate;
import java.util.List;

public record SlotFindResponse(
        Long id,
        LocalDate date,
        Long theme_id,
        Long time_id
) {
    public static SlotFindResponse from(Slot slot) {
        return new SlotFindResponse(
                slot.getId(),
                slot.getDate(),
                slot.getThemeId(),
                slot.getTimeId()
        );
    }

    public static List<SlotFindResponse> from(List<Slot> slots) {
        return slots.stream()
                .map(SlotFindResponse::from)
                .toList();
    }
}
