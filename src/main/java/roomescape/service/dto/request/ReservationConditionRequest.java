package roomescape.service.dto.request;

import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ReservationConditionRequest(
        @Positive(message = "테마 아이디는 양수여야 해요")
        Long themeId,

        @Positive(message = "멤버 아이디는 양수여야 해요")
        Long memberId,

        LocalDate dateFrom,

        LocalDate dateTo
) {
}
