package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;
import roomescape.repository.dto.TimeSlotProjection;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(long id);

    boolean existsByStartAt(LocalTime time);

    List<ReservationTime> findAllTimes();

    List<TimeSlotProjection> findTimesByThemeWithReservationStatus(long themeId, LocalDate date);

    void update(ReservationTime time);
}
