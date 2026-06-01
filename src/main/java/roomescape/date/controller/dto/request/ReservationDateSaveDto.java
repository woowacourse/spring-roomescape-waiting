package roomescape.date.controller.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationDateSaveDto(
    @NotNull(message = "date는 필수 입력입니다.")
    LocalDate date
) {

}
