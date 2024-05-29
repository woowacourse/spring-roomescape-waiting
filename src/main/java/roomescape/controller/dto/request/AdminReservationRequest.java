package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import roomescape.service.dto.request.ReservationCreationRequest;

public record AdminReservationRequest(
        @NotNull(message = "날짜는 필수 값입니다.")
        LocalDate date,

        @NotNull(message = "테마 id는 필수 값입니다.")
        @Positive
        Long themeId,

        @NotNull(message = "시간 id는 필수 값입니다.")
        @Positive
        Long timeId,

        @NotNull(message = "회원 id는 필수 값입니다.")
        @Positive
        Long memberId
) {

    public ReservationCreationRequest toReservationCreationRequest() {
        return new ReservationCreationRequest(date, timeId, themeId, memberId);
    }
}
