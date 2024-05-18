package roomescape.reservation.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ReservationConditionSearchRequest(
        @NotNull(message = "예약자 정보가 없습니다.")
        Long memberId,
        @NotNull(message = "테마가 선택되지 않았습니다.")
        Long themeId,
        @NotNull(message = "시작 날짜 선택되지 않았습니다.")
        LocalDate dateFrom,
        @NotNull(message = "마지막 날짜 선택되지 않았습니다.")
        LocalDate dateTo
) {

}
