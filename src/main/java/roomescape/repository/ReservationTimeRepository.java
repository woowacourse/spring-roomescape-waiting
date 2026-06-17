package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    ReservationTime findReservationTimeById(Long id);

    List<ReservationTime> findAll();

    Boolean existsReservationTimeById(Long id);

    Boolean existsReservationTimeByStartAt(LocalTime startAt);

    ReservationTime save();

    void deleteById(Long id);
}
