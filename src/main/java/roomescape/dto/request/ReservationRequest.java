package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank(message = "예약자명은 필수값 입니다.")
        @Size(max = 10, message = "예약자명은 10자 이하여야 합니다.")
        String name,

        @NotNull(message = "예약 날짜는 필수값 입니다.")
        LocalDate date,

        long timeId,
        long themeId
) {
    
}
