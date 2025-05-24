package roomescape.domain.time;

import roomescape.domain.reservation.schdule.ReservationSchedule;

public record AvailableReservationTime(
        ReservationSchedule schedule,
        boolean available
) {
    public AvailableReservationTime(final ReservationSchedule schedule, final boolean available) {
        this.schedule = schedule;
        this.available = available;
    }
}
