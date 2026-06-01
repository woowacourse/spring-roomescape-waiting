package roomescape.slot.controller.dto.request;

import roomescape.slot.service.dto.SlotSaveCommand;

// TODO validation 추가
public record SlotSaveDto(
        Long dateId,
        Long timeId,
        Long themeId
) {

    public SlotSaveCommand toCommand() {
        return new SlotSaveCommand(dateId, timeId, themeId);
    }

}
