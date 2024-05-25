package roomescape.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.application.dto.request.ReservationRequest;

public record AdminReservationWebRequest(
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

    public ReservationRequest toReservationRequest(Clock clock) {
        return new ReservationRequest(LocalDateTime.now(clock), date, themeId, timeId, memberId);
    }
}
