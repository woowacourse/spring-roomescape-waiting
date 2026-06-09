package roomescape.slot.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.slot.service.dto.SlotSaveCommand;

public record SlotSaveDto(

        @NotNull(message = "dateId는 필수 입력값입니다.")
        Long dateId,

        @NotNull(message = "timeId는 필수 입력값입니다.")
        Long timeId,

        @NotNull(message = "themeId는 필수 입력값입니다.")
        Long themeId

) {

    public SlotSaveCommand toCommand() {
        return new SlotSaveCommand(dateId, timeId, themeId);
    }

}
