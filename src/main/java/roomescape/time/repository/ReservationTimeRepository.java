package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);
}
