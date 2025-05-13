package roomescape.reservation.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.reservation.service.dto.ReservationCreateCommand;

public record ReservationCreateRequest(
        @NotNull(message = "날짜를 입력해주세요.") LocalDate date,
        @NotNull(message = "시간을 입력해주세요.") Long timeId,
        @NotNull(message = "테마를 입력해주세요.") Long themeId
) {

    public ReservationCreateCommand convertToCreateCommand(final long memberId) {
        return new ReservationCreateCommand(date, memberId, timeId, themeId);
    }
}
