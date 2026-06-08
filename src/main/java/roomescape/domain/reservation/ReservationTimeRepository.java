package roomescape.domain.reservation;

import java.util.List;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    int deleteById(Long id);
}
