package roomescape.dto.reservationmember;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationMemberResponseDto(Long id, String memberName, String themeName, LocalDate date,
                                           LocalTime startAt) {
}
