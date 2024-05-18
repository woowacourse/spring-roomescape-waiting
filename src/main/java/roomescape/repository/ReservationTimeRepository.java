package roomescape.repository;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationTime;
import roomescape.service.exception.TimeNotFoundException;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);

    default ReservationTime findByIdOrThrow(long id) {
        return findById(id).orElseThrow(() -> new TimeNotFoundException("존재하지 않는 시간입니다."));
    }
}
