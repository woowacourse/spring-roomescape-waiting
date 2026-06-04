package roomescape.service.dto.response;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.repository.dto.ReservationTimeDto;

public record ServiceReservationTimeResponse(
        Long id,
        LocalTime startAt
) {
    public static ServiceReservationTimeResponse from(ReservationTime reservationTime) {
        return new ServiceReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }

    public static ServiceReservationTimeResponse from(ReservationTimeDto reservationTimeDto) {
        return new ServiceReservationTimeResponse(
                reservationTimeDto.id(),
                reservationTimeDto.startAt()
        );
    }
}
