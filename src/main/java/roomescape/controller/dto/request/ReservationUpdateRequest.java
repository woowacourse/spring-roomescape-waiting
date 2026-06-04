package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ReservationUpdateRequest(

        @NotBlank(message = "name은 비어 있을 수 없습니다.")
        @Size(max = 255, message = "name은 255자를 넘을 수 없습니다.")
        String name,

        LocalDate date,

        @Positive(message = "timeId는 양수이어야 합니다.")
        Long timeId) {
}
