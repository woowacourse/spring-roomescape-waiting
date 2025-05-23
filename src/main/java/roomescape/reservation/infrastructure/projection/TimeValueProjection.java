package roomescape.reservation.infrastructure.projection;

import roomescape.timeslot.domain.ReservationTime;

public interface TimeValueProjection {

    ReservationTime getTime();
}
