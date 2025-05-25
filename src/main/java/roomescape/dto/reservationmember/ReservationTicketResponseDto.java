package roomescape.dto.reservationmember;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationTicketResponseDto(Long id, String memberName, String themeName, LocalDate date,
                                           LocalTime startAt) {
}
