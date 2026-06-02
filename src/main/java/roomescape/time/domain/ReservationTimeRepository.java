package roomescape.time.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long id);

    Optional<ReservationTime> findByIdForUpdate(Long id);

    boolean existsByStartAt(LocalTime localTime);

    List<ReservationTime> findAll();

    List<AvailableTimeQueryResult> findAvailableTimes(Long themeId, LocalDate date);

    void delete(ReservationTime time);
}
