package roomescape.waiting.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateWaitingRequest(
        @FutureOrPresent(message = "예약 대기 날짜는 현재보다 과거일 수 없습니다.")
        @NotNull(message = "예약 대기 등록 시 예약 날짜는 필수입니다.")
        LocalDate date,

        @Positive(message = "예약 대기 시간 식별자는 양수만 가능합니다.")
        @NotNull(message = "예약 대기 등록 시 시간은 필수입니다.")
        Long timeId,

        @Positive(message = "예약 대기 테마 식별자는 양수만 가능합니다.")
        @NotNull(message = "예약 대기 등록 시 테마는 필수입니다.")
        Long themeId) {
}
