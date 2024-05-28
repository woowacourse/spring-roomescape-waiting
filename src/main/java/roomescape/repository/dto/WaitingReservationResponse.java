package roomescape.repository.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingReservationResponse(long id, String name, String theme, LocalDate date, LocalTime time) {
}
