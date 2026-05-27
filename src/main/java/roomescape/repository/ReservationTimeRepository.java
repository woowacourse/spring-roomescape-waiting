package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTimeWithoutId);

    Optional<ReservationTime> findById(Long id);

    List<Long> findReservedTimeIdByDateAndTheme(LocalDate date, Long themeId);

    List<ReservationTime> findAll();

    void delete(Long id);

    boolean existByStartAt(LocalTime startAt);
}
