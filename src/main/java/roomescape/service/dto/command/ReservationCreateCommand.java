package roomescape.service.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReservationCreateCommand(
        @NotNull(message = "예약자 이름은 비워둘 수 없습니다.")
        @NotBlank(message = "예약자 이름은 비워둘 수 없습니다.")
        String name,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "예약 날짜는 비워둘 수 없습니다.")
        LocalDate date,

        @NotNull(message = "예약 시간 ID는 비워둘 수 없습니다.")
        Long timeId,

        @NotNull(message = "예약 테마 ID는 비워둘 수 없습니다.")
        Long themeId
) {
}
