package roomescape.slot.controller.dto.response;

import roomescape.slot.domain.ReservationSlot;

import java.time.LocalDate;
import java.time.LocalTime;

public record SlotDetailDto(
        Long id,
        LocalDate date,
        LocalTime startAt,
        String themeName,
        String description,
        String thumbnailUrl
) {

    public static SlotDetailDto from(ReservationSlot slot) {
        return new SlotDetailDto(
                slot.getId(),
                slot.getDate().getDate(),
                slot.getTime().getStartAt(),
                slot.getTheme().getName(),
                slot.getTheme().getDescription(),
                slot.getTheme().getThumbnailUrl()
        );
    }

}
