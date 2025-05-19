package roomescape.dto.request;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record AdminReservationRequest(
        long memberId,
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        long themeId,
        long timeId
) {
}
