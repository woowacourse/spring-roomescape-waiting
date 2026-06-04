package roomescape.time.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.time.application.dto.AvailableReservationTimeFindCommand;

public record AvailableReservationTimeRequest(
        @NotNull(message = "테마 ID는 필수입니다.")
        Long themeId,
        @NotNull(message = "날짜는 필수입니다.")
        LocalDate date
) {
    public AvailableReservationTimeFindCommand toCommand() {
        return AvailableReservationTimeFindCommand.builder()
                .themeId(this.themeId)
                .date(this.date)
                .build();
    }
}
