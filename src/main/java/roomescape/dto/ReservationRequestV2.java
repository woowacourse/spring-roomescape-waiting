package roomescape.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record ReservationRequestV2(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        long themeId,
        long timeId
) {
}
