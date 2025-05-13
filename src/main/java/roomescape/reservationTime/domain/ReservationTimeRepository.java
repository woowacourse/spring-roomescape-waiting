package roomescape.reservationTime.domain;

import java.util.List;

public interface ReservationTimeRepository {

    Long save(ReservationTime reservationTime);

    boolean deleteById(Long id);

    ReservationTime findById(Long id);

    List<ReservationTime> findAll();
}
