package roomescape.booking.dto;

import roomescape.booking.reservation.Reservation;
import roomescape.booking.waiting.Waiting;
import roomescape.schedule.dto.ScheduleResponse;

public record BookingResponse(
        Long id,
        ScheduleResponse schedule,
        String status
) {

    public static BookingResponse of(Reservation reservation) {
        return new BookingResponse(reservation.getId(), ScheduleResponse.of(reservation.getSchedule()), "예약");
    }

    public static BookingResponse of(Waiting waiting, Long rank) {
        return new BookingResponse(waiting.getId(), ScheduleResponse.of(waiting.getSchedule()), rank + "번째 예약대기");
    }
}
