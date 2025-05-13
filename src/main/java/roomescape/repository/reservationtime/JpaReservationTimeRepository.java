package roomescape.repository.reservationtime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationtime.ReservationTime;

@Repository
public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long>, ReservationTimeRepository {

    long add(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    void deleteById(Long id);

    Optional<ReservationTime> findById(Long id);

    boolean existsByTime(LocalTime time);
}
