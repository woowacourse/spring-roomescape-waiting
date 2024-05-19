package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);

    boolean existsByStartAt(LocalTime startAt);

    Optional<ReservationTime> findById(long id);

    List<ReservationTime> findAll();

    List<ReservationTime> findUsedTimeByDateAndTheme(LocalDate date, Theme theme);

    void delete(long id);
}
