package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.reservation.application.dto.ReservationChangeCommand;

public record ReservationChangeRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String username,
        @NotNull(message = "날짜는 필수입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull(message = "시간 ID는 필수입니다.")
        Long timeId,
        @NotNull(message = "테마 ID는 필수입니다.")
        Long themeId
) {
    public ReservationChangeCommand toCommand() {
        return ReservationChangeCommand.builder()
                .username(this.username)
                .timeId(this.timeId)
                .themeId(this.themeId)
                .date(this.date)
                .build();
    }
}
