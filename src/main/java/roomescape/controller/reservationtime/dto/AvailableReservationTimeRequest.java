package roomescape.controller.reservationtime.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

public record AvailableReservationTimeRequest(
        @NotNull(message = "날짜는 필수입니다.")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date
) {
}
