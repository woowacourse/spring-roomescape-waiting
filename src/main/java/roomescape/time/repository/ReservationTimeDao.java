package roomescape.time.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

public interface ReservationTimeDao {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime localTime);

    List<ReservationTime> findAll();

    List<AvailableTimeQueryResult> queryAvailableTimes(Long themeId, LocalDate date);

    void delete(ReservationTime time);
}
