package roomescape.time.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.time.domain.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long id);

    boolean existByStartAt(LocalTime localTime);

    List<ReservationTime> findAll();

    List<AvailableTimeQueryResult> findAvailableTimes(Long themeId, LocalDate date);

    int deleteById(Long id);
}
