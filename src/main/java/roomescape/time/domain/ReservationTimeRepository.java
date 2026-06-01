package roomescape.time.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.common.exception.NotFoundException;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll(int page, int size);

    List<ReservationTime> findAllActive();

    Optional<ReservationTime> findById(Long id);

    boolean existsActiveByStartAt(LocalTime time);

    void update(ReservationTime time);

    default ReservationTime getById(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 시간대입니다."));
    }
}
