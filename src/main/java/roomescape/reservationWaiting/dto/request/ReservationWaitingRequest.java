package roomescape.reservationWaiting.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationWaitingRequest(
        @NotBlank(message = "예약 대기자명은 필수값 입니다.")
        String name,

        @NotNull(message = "예약 날짜는 필수값 입니다.")
        LocalDate reservationDate,

        @NotNull(message = "예약 시간은 필수값 입니다.")
        long timeId,

        @NotNull(message = "예약 테마는 필수값 입니다.")
        long themeId
) {
}
