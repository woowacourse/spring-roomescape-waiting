package roomescape.dto.reservationmember;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationMemberResponseDto(Long reservationId, String memberName, String themeName, LocalDate date,
                                           LocalTime startAt) {
}
