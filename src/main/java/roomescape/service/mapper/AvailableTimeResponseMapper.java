package roomescape.service.mapper;

import java.util.Set;
import roomescape.domain.ReservationTime;
import roomescape.dto.AvailableTimeResponse;

public class AvailableTimeResponseMapper {
    public static AvailableTimeResponse toResponse(Set<ReservationTime> alreadyUsedTimes,
                                                   ReservationTime reservationTime) {
        boolean isBooked = alreadyUsedTimes.contains(reservationTime);
        long id = reservationTime.getId();
        return new AvailableTimeResponse(id, reservationTime.getStartAt(), isBooked);
    }
}
