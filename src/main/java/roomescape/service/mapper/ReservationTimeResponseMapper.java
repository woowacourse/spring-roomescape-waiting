package roomescape.service.mapper;

import roomescape.domain.ReservationTime;
import roomescape.dto.ReservationTimeResponse;

public class ReservationTimeResponseMapper {
    public static ReservationTimeResponse toResponse(ReservationTime saved) {
        return new ReservationTimeResponse(saved.getId(), saved.getStartAt());
    }
}
