package roomescape.reservation.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime startAt);

    List<ReservationTime> findAll();

    void deleteById(Long id);
}
