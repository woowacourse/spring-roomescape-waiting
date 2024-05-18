package roomescape.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import roomescape.application.dto.request.ReservationWaitingRequest;

public record ReservationWaitingWebRequest(
        @NotNull(message = "날짜를 입력해주세요.")
        LocalDate date,

        @NotNull(message = "예약 시간 id을 입력해주세요.")
        @Positive
        Long timeId,

        @NotNull(message = "테마 id을 입력해주세요.")
        @Positive
        Long themeId
) {

    public ReservationWaitingRequest toReservationWaitingRequest(Long memberId) {
        return new ReservationWaitingRequest(date, timeId, themeId, memberId);
    }
}
