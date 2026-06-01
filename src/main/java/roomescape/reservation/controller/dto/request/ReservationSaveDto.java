package roomescape.reservation.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.reservation.service.dto.ReservationSaveCommand;

public record ReservationSaveDto(

    @NotNull(message = "dateId는 필수 입력값입니다.")
    Long dateId,

    @NotNull(message = "timeId는 필수 입력값입니다.")
    Long timeId,

    @NotNull(message = "themeId는 필수 입력값입니다.")
    Long themeId

) {

    public ReservationSaveCommand toCommand() {
        return new ReservationSaveCommand(dateId, timeId, themeId);
    }

}
