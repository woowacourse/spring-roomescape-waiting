package roomescape.booking.dto;

import roomescape.booking.reservation.Reservation;
import roomescape.booking.schedule.dto.ScheduleResponse;
import roomescape.booking.waiting.Waiting;

public record BookingResponse(
        Long id,
        ScheduleResponse schedule,
        String status
) {

    public static BookingResponse of(Reservation reservation) {
        return new BookingResponse(reservation.getId(), ScheduleResponse.of(reservation.getSchedule()), "예약");
    }

    public static BookingResponse of(Waiting waiting) {
        return new BookingResponse(waiting.getId(), ScheduleResponse.of(waiting.getSchedule()), waiting.getRank() + "번째 예약대기");
    }
}
