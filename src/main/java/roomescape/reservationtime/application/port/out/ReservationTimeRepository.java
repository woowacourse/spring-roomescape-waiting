package roomescape.reservationtime.application.port.out;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime time);

    List<ReservationTime> findAll();

    void deleteById(Long id);

    Optional<ReservationTime> findById(long id);

    List<ReservationTime> findTimesByDateAndThemeId(LocalDate date, long themeId);

    boolean existsAlreadyTime(LocalTime startAt);
}
