package roomescape.reservationtime.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record AvailableReservationTimeRequest(
        @NotNull(message = "날짜는 필수입니다.")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date
) {
}
