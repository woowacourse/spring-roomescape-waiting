package roomescape.controller.dto;

import java.time.LocalDate;

public record SearchReservationFilterRequest(Long themeId,
                                             Long memberId,
                                             LocalDate dateFrom,
                                             LocalDate dateTo) { }
