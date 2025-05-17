package roomescape.dto.reservationmember;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationMemberResponseDto(Long reservationId, String name, String themeName,
                                             LocalDate date, LocalTime startAt, String status) {
}
