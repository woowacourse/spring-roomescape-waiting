package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ReservationTimeBookedRequest(
        @NotNull(message = "예약 날짜가 존재하지 않습니다")
        LocalDate date,

        @NotNull(message = "테마 아이디가 존재하지 않습니다")
        @Positive(message = "테마 아이디는 1이상의 양수여야 해요")
        Long themeId
) {
}
