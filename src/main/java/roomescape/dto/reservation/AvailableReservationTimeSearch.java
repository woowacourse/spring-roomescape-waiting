package roomescape.dto.reservation;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public record AvailableReservationTimeSearch(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        Long themeId
) {
}
