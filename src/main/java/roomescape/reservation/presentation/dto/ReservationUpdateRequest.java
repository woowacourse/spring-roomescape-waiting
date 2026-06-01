package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.reservation.application.dto.ReservationUpdateCommand;

public record ReservationUpdateRequest(
        @NotNull(message = "날짜는 비어있을 수 없습니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull(message = "시간은 비어있을 수 없습니다.")
        @Positive(message = "시간ID는 양수여야 합니다.")
        Long timeId
) {
    public ReservationUpdateCommand toCommand(LocalDateTime now) {
        return new ReservationUpdateCommand(date, timeId, now);
    }
}
