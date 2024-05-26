package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.time.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    default ReservationTime getById(Long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 시간입니다."));
    }

    List<ReservationTime> findByIdNotIn(List<Long> ids);

    boolean existsByStartAt(LocalTime time);
}
