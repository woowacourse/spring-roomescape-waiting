package roomescape.slot.application.dto.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.slot.domain.Slot;

public record SlotFindResponse(
        Long id,
        LocalDate date,
        Long theme_id,
        Long time_id,
        int price
) {
    public SlotFindResponse(Long id, LocalDate date, Long theme_id, Long time_id) {
        this(id, date, theme_id, time_id, 0);
    }

    public static SlotFindResponse from(Slot slot) {
        return new SlotFindResponse(
                slot.getId(),
                slot.getDate(),
                slot.getThemeId(),
                slot.getTimeId(),
                slot.getPrice()
        );
    }

    public static List<SlotFindResponse> from(List<Slot> slots) {
        return slots.stream()
                .map(SlotFindResponse::from)
                .toList();
    }
}
