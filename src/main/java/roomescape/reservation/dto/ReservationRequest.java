package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank(message = "이름은 비어 있을 수 없습니다.")
        String name,

        @NotNull(message = "예약 날짜는 필수입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @NotNull(message = "시간 지정은 필수입니다.")
        @Positive(message = "시간 id는 양수여야 합니다.")
        Long timeId,

        @NotNull(message = "테마 지정은 필수입니다.")
        @Positive(message = "테마 id는 양수여야 합니다.")
        Long themeId
) {

}
