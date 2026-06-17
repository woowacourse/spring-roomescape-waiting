package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Override
    @Query("SELECT r FROM ReservationTime r ORDER BY r.startAt")
    List<ReservationTime> findAll();

    boolean existsById(Long id);
}
