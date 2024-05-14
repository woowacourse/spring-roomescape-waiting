package roomescape.web.dto.request.reservation;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationSearchCond(
        @NotNull(message = "시작일은 비어있을 수 없습니다.") LocalDate start,
        @NotNull(message = "종료일은 비어있을 수 없습니다.") LocalDate end,
        @NotBlank(message = "예약자명은 비어있을 수 없습니다.") String memberName,
        @NotBlank(message = "테마명은 비어있을 수 없습니다.") String themeName
) {

}
