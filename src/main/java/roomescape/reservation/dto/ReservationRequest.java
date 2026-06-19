package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull
        Long timeId,
        @NotNull
        Long themeId,
        @NotBlank
        String orderId,
        @NotNull
        Long amount) {
}
