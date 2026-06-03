package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateReservationRequest(
        @NotNull(message = "날짜가 입력되지 않았습니다. 날짜를 입력해주세요.")
        LocalDate date,

        @NotNull(message = "예약 시간이 선택되지 않았습니다. 시간을 선택해주세요.")
        Long timeId
) {
}
