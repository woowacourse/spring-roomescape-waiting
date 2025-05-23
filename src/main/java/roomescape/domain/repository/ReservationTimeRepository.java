package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;
import roomescape.persistence.dto.ReservationTimeAvailabilityData;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long reservationTimeId);

    List<ReservationTimeAvailabilityData> findAvailableTimesByThemeAndDate(Long themeId, LocalDate reservationDate);

    void deleteById(Long reservationTimeId);
}
