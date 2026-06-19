package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.reservation.application.dto.ReservationApplicationCreateCommand;

public record ReservationApplicationCreateRequest(
        @NotBlank(message = "이름은 비어있을 수 없습니다.")
        String name,
        @NotNull(message = "날짜는 비어있을 수 없습니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull(message = "테마는 비어있을 수 없습니다.")
        @Positive(message = "테마ID는 양수여야 합니다.")
        Long themeId,
        @NotNull(message = "시간은 비어있을 수 없습니다.")
        @Positive(message = "시간ID는 양수여야 합니다.")
        Long timeId,
        String paymentKey,
        Long amount
) {
    public ReservationApplicationCreateCommand toCommand(LocalDateTime now) {
        return new ReservationApplicationCreateCommand(name, date, themeId, timeId, paymentKey, amount, now);
    }
}
