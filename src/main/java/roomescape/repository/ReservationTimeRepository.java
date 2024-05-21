package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationTime;
import roomescape.service.exception.TimeNotFoundException;

import java.time.LocalTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    default ReservationTime fetchById(long id) {
        return findById(id).orElseThrow(() -> new TimeNotFoundException("존재하지 않는 시간입니다."));
    }

    boolean existsByStartAt(LocalTime startAt);
}
