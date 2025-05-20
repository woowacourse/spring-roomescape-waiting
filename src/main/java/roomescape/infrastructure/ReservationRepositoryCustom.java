package roomescape.infrastructure;

import java.util.List;
import roomescape.domain.Reservation;
import roomescape.dto.request.ReservationCondition;

public interface ReservationRepositoryCustom {

    List<Reservation> findByCondition(ReservationCondition condition);
}
