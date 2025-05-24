package roomescape.booking.reservation.dto;

import roomescape.booking.reservation.Reservation;
import roomescape.booking.schedule.dto.ScheduleResponse;
import roomescape.member.dto.MemberResponse;

public record ReservationResponse(
        Long id,
        ScheduleResponse schedule,
        MemberResponse member
) {

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                ScheduleResponse.of(reservation.getSchedule()),
                MemberResponse.from(reservation.getMember())
        );
    }
}
