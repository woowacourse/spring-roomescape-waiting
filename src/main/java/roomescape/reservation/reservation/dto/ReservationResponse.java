package roomescape.reservation.reservation.dto;

import roomescape.member.dto.MemberResponse;
import roomescape.reservation.reservation.Reservation;
import roomescape.schedule.dto.ScheduleResponse;

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
