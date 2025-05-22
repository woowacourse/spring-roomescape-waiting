package roomescape.reservation.reservation.dto;

import roomescape.reservation.reservation.Reservation;
import roomescape.reservation.waiting.Waiting;
import roomescape.schedule.dto.ScheduleResponse;

public record ReservationAndWaitingResponse(
        Long id,
        ScheduleResponse schedule,
        String status
) {

    public static ReservationAndWaitingResponse of(Reservation reservation) {
        return new ReservationAndWaitingResponse(reservation.getId(), ScheduleResponse.of(reservation.getSchedule()), "예약");
    }

    public static ReservationAndWaitingResponse of(Waiting waiting) {
        return new ReservationAndWaitingResponse(waiting.getId(), ScheduleResponse.of(waiting.getSchedule()), waiting.getRank() + "번째 예약대기");
    }
}
