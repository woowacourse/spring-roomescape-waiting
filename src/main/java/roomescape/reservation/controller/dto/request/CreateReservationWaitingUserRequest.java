package roomescape.reservation.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.reservation.application.dto.request.CreateReservationWaitingServiceRequest;

public record CreateReservationWaitingUserRequest(
        @NotNull(message = "날짜를 필수로 입력해야 합니다.")
        LocalDate date,
        @NotNull(message = "시간을 필수로 입력해야 합니다.")
        Long timeId,
        @NotNull(message = "테마를 필수로 입력해야 합니다.")
        Long themeId
) {

    public CreateReservationWaitingServiceRequest toServiceRequest(Long memberId) {
        return new CreateReservationWaitingServiceRequest(memberId, date, timeId, themeId);
    }
}
