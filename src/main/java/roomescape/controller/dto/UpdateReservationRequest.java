package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateReservationRequest(
        @NotNull(message = "날짜는 필수입니다.")
        LocalDate date,

        @NotNull(message = "시간은 필수입니다.")
        Long timeId
) {
}
