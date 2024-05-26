package roomescape.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.application.dto.request.WaitingRequest;

public record WaitingWebRequest(
        @NotNull(message = "날짜를 입력해주세요.")
        LocalDate date,

        @NotNull(message = "예약 시간 id을 입력해주세요.")
        @Positive
        Long timeId,

        @NotNull(message = "테마 id을 입력해주세요.")
        @Positive
        Long themeId
) {

    public WaitingRequest toWaitingRequest(Clock clock, Long memberId) {
        return new WaitingRequest(LocalDateTime.now(clock), date, themeId, timeId, memberId);
    }
}
