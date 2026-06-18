package roomescape.controller.reservationwaiting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ReservationWaitingCreateRequest(
        @NotBlank(message = "예약자 이름은 비어 있을 수 없습니다.")
        @Size(max = 9, message = "예약자 이름은 10자 미만이어야 합니다.")
        String name,

        @NotNull(message = "날짜는 필수입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @NotNull(message = "themeId는 필수입니다.")
        Long themeId,

        @NotNull(message = "timeId는 필수입니다.")
        Long timeId
) {

}
