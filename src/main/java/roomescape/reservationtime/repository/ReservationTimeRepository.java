package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservationtime.model.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long timeId);

    boolean existsById(Long id);

    boolean existsByStartAt(LocalTime time);

    void deleteById(Long id);
}
