package roomescape.controller.admin.api.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import roomescape.application.service.command.ReservationCommand;

public record AdminReservationRequest(
        @NotBlank(message = "예약자 이름 정보는 비어있을 수 없습니다.")
        String name,
        @NotNull(message = "예약 날짜 정보는 필수 값입니다.")
        @FutureOrPresent(message = "이미 지난 날짜는 예약할 수 없습니다.")
        LocalDate date,
        @NotNull(message = "테마 식별자는 필수 값입니다.")
        @Positive(message = "테마 식별자는 식별 가능한 양수입니다.")
        Long themeId,
        @NotNull(message = "예약 시간 식별자는 필수 값입니다.")
        @Positive(message = "예약 시간 식별자는 식별 가능한 양수입니다.")
        Long timeId
) {

    public ReservationCommand toCommand() {
        return new ReservationCommand(name, date, themeId, timeId);
    }
}
