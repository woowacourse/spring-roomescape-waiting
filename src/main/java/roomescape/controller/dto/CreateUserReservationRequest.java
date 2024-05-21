package roomescape.controller.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record CreateUserReservationRequest(
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate date,
    Long themeId,
    Long timeId
) { }
