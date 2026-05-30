package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationChangeRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        @NotNull(message = "변경 시간은 필수로 입력해야 합니다.")
        Long themeId,
        @NotNull(message = "변경 날짜는 필수로 입력해야 합니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull(message = "변경 시간은 필수로 입력해야 합니다.")
        Long timeId
) {
}
