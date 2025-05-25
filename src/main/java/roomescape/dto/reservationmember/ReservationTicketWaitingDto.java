package roomescape.dto.reservationmember;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationTicketWaitingDto(Long id, String name, String themeName,
                                          LocalDate date, LocalTime startAt, String status, int waitRank) {
}
