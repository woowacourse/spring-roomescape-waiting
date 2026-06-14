package roomescape.controller.reservationslot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationSlotCreateRequest(
        @NotNull(message = "예약 날짜는 필수입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @NotNull(message = "themeId는 필수입니다.")
        Long themeId,

        @NotNull(message = "timeId는 필수입니다.")
        Long timeId
) {
}
