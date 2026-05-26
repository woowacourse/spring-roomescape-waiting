package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    List<ReservationTime> findAll(int limit, int offset);

    Optional<ReservationTime> findById(Long id);

    Long save(ReservationTime reservationTime);

    int deleteById(Long id);
}