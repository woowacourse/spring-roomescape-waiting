package roomescape.service.dto.result;

import java.time.LocalDate;
import java.util.List;

public record AvailableDateResult(
        List<LocalDate> dates
) {
}
