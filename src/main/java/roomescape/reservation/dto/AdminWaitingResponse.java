package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AdminWaitingResponse(long id, String memberName, String themeName, LocalDate date,
                                   LocalTime time) {
}
