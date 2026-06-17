package roomescape.service.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationDeleteCommand(
        @NotNull(message = "예약 ID는 비워둘 수 없습니다.")
        Long reservationId,

        @NotNull(message = "예약자 이름은 비워둘 수 없습니다.")
        @NotBlank(message = "예약자 이름은 비워둘 수 없습니다.")
        String name
) {
}
