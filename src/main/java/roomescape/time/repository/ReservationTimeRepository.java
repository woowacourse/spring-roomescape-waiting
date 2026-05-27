package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.time.domain.ReservationTime;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    ReservationTime save(ReservationTime reservationTime);

    boolean updateStatus(ReservationTime reservationTime);

    boolean existsByStartAt(LocalTime startAt);

    List<ReservationTime> findAvailableByDateIdAndThemeId(Long dateId, Long themeId);
}
