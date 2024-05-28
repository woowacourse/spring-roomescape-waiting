package roomescape.service.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record WaitingRequest(
        @NotNull(message = "날짜를 입력해주세요")
        @FutureOrPresent(message = "지나간 날짜의 예약을 할 수 없습니다.")
        LocalDate date,

        @NotNull(message = "시간을 선택해주세요")
        @Positive(message = "잘못된 시간입력이 들어왔습니다.")
        Long timeId,

        @NotNull(message = "테마를 선택해주세요")
        @Positive(message = "잘못된 테마입력이 들어왔습니다")
        Long themeId
) {
}

