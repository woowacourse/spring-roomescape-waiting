package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReservationModifyRequest(
        @NotNull(message = "예약자 이름은 비워둘 수 없습니다.")
        @NotBlank(message = "예약자 이름은 비워둘 수 없습니다.")
        String name,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        Long timeId
) {
}
