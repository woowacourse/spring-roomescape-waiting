package roomescape.reservationTime.dao;

import java.time.LocalTime;
import roomescape.common.Dao;
import roomescape.reservationTime.domain.ReservationTime;

public interface ReservationTimeDao extends Dao<ReservationTime> {
    Boolean existsByStartAt(LocalTime startAt);

    Boolean existsByReservationTimeId(Long timeId);
}
