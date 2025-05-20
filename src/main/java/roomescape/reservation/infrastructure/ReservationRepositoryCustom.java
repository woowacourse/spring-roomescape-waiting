package roomescape.reservation.infrastructure;

import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.request.ReservationCondition;

public interface ReservationRepositoryCustom {

    List<Reservation> findByCondition(ReservationCondition condition);
}
