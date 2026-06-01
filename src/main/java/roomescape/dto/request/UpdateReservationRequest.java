package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateReservationRequest(
        @NotNull(message = "수정할 예약 날짜는 필수값 입니다.")
        LocalDate date,

        @NotNull(message = "예약 시간은 필수값 입니다.")
        Long timeId
) {
}
