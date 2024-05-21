package roomescape.controller.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record SearchReservationFilterRequest(
    Long themeId,
    Long memberId,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate dateFrom,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate dateTo
) { }
