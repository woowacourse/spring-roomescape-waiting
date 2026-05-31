package roomescape.reservation.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.reservation.service.dto.ReservationChangeCommand;

public record ReservationChangeScheduleDto(
        @NotNull(message = "dateId는 필수 입력값입니다.")
        Long dateId,

        @NotNull(message = "timeId는 필수 입력값입니다.")
        Long timeId
) {
    public ReservationChangeCommand toCommand(Long id, String requesterName) {
        return new ReservationChangeCommand(id, requesterName, dateId, timeId);
    }

    public ReservationChangeCommand toCommand(Long id) {
        return new ReservationChangeCommand(id, null, dateId, timeId);
    }
}
