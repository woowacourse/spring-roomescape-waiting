package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.reservation.application.dto.ReservationUpdateCommand;

public record ReservationUpdateRequest(
        @NotBlank(message = "[ERROR] 이름은 비어있을 수 없습니다.")
        String name,
        @NotNull(message = "[ERROR] 날짜는 비어있을 수 없습니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull(message = "[ERROR] 시간은 비어있을 수 없습니다.")
        Long timeId
) {
    public ReservationUpdateCommand toCommand(Long id) {
        return new ReservationUpdateCommand(id, name, date, timeId);
    }
}
