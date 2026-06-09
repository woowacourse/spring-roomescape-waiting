package roomescape.reservation.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.reservation.service.dto.ReservationCommand;

public record ReservationRequest(
        @NotBlank(message = "예약자 이름을 입력해주세요.")
        String name,

        @NotNull(message = "예약 날짜를 입력해주세요.")
        LocalDate date,

        @NotNull(message = "예약 시간을 선택해주세요.")
        Long timeId,

        @NotNull(message = "테마를 선택해주세요.")
        Long themeId
) {
    public ReservationCommand toCommand() {
        return new ReservationCommand(
                name,
                date,
                timeId,
                themeId
        );
    }
}
