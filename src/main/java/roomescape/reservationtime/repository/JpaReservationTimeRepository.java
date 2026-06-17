package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservationtime.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(long id);

    int deleteById(long id);

    ReservationTime save(ReservationTime reservationTime);

    boolean existsByStartAt(LocalTime startAt);
}
