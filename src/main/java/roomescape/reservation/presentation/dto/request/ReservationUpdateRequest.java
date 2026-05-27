package roomescape.reservation.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationUpdateRequest(
        @NotNull(message = "예약 날짜를 선택해 주세요.")
        LocalDate date,

        @NotNull(message = "예약 시간을 선택해 주세요.")
        Long timeId,

        @NotBlank(message = "예약자 이름을 입력해 주세요.")
        String name
) {
}

