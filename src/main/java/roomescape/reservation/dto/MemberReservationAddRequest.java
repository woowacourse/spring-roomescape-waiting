package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record MemberReservationAddRequest(
        @NotNull(message = "예약 날짜는 필수 입니다.") LocalDate date,
        @NotNull(message = "예약 시간 선택은 필수 입니다.") @Positive Long timeId,
        @NotNull(message = "테마 선택은 필수 입니다.") @Positive Long themeId) {
}
