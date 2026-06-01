package roomescape.slot.service.dto;

public record SlotSaveCommand(
        Long dateId,
        Long timeId,
        Long themeId
) {
}
