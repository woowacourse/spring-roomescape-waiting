package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import roomescape.service.dto.ReservationCreateCommand;

public record ReservationRequest(
        @NotBlank(message = "예약자 이름은 필수입니다") String reserverName,
        @NotNull(message = "예약 날짜는 필수입니다")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @NotNull(message = "시간 ID는 필수입니다")
        @Positive(message = "시간 ID는 1 이상이여야 합니다") Long timeId,
        @NotNull(message = "테마 ID는 필수입니다")
        @Positive(message = "테마 ID는 1 이상이여야 합니다") Long themeId
) {
    public ReservationCreateCommand toCommand() {
        return new ReservationCreateCommand(reserverName, date, timeId, themeId);
    }
}
