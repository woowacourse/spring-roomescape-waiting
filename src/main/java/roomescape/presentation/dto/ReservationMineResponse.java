package roomescape.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationMineResponse(Long reservationId, String theme, LocalDate date, LocalTime time, String status,
                                      Long waitInfoId) {
}
