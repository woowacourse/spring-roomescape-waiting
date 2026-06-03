package roomescape.waiting.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import roomescape.waiting.service.dto.ReservationWaitingCommand;

public record ReservationWaitingRequest(

        @NotBlank(message = "예약자 이름을 입력해주세요.")
        String name,

        @NotNull(message = "예약 날짜를 입력해주세요.")
        LocalDate date,

        @NotNull(message = "예약 시간을 선택해주세요.")
        @Positive(message = "올바른 예약 시간을 선택해주세요.")
        Long timeId,

        @NotNull(message = "테마를 선택해주세요.")
        @Positive(message = "올바른 테마를 선택해주세요.")
        Long themeId
) {
    public ReservationWaitingCommand toCommand() {
        return new ReservationWaitingCommand(
                name,
                date,
                timeId,
                themeId
        );
    }
}
