package roomescape.reservation.repository;

import java.time.LocalTime;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.exceptions.NotFoundException;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime localTime);

    default ReservationTime getById(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 예약 시간입니다. reservationTimeId = " + id));
    }
}
