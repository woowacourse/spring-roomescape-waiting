package roomescape.time.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.time.application.exception.ReservationTimeNotFoundException;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);
    List<ReservationTime> findAll();
    Optional<ReservationTime> findById(Long id);
    boolean existsByStartAt(LocalTime time);
    int delete(ReservationTime time);

    default ReservationTime getById(Long id) {
        return findById(id).orElseThrow(() -> new ReservationTimeNotFoundException("존재하지 않는 시간ID 입니다."));
    }
}
