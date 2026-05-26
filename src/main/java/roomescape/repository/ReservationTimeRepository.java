package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime create(ReservationTime reservationTimeWithoutId);

    Optional<ReservationTime> read(Long id);

    List<ReservationTime> readAll();

    void delete(Long id);

    List<Long> reservedTimeIdByDateAndTheme(LocalDate date, Long themeId);

    boolean existByStartAt(LocalTime startAt);
}
