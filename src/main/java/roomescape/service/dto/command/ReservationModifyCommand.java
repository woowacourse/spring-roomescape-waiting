package roomescape.service.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReservationModifyCommand(
        @NotNull(message = "수정할 예약 ID는 비워둘 수 없습니다.")
        Long reservationId,

        @NotNull(message = "예약자 이름은 비워둘 수 없습니다.")
        @NotBlank(message = "예약자 이름은 비워둘 수 없습니다.")
        String name,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        Long timeId
) {
}
