package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.time.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    List<ReservationTime> findAllByIsActiveTrue();

    boolean existsByStartAt(LocalTime startAt);
//    List<ReservationTime> findAll();
//
//    Optional<ReservationTime> findById(Long id);
//
//    ReservationTime save(ReservationTime reservationTime);
//
//    boolean updateStatus(ReservationTime reservationTime);
//
//    boolean existsByStartAt(LocalTime startAt);
//
//    List<ReservationTime> findAvailableByDateIdAndThemeId(Long dateId, Long themeId);
}
