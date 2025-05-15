package roomescape.reservationtime.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    void deleteById(Long id);

    List<ReservationTime> findByReservationDateAndThemeId(LocalDate date, Long themeId);
}
