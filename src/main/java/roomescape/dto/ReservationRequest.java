package roomescape.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record ReservationRequest(
        String name,
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        long themeId,
        long timeId) {
}
