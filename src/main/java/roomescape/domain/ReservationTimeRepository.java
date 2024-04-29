package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findByReservationDateAndThemeId(LocalDate date, Long themeId);

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);
}
