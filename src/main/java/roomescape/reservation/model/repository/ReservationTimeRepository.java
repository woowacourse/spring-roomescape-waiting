package roomescape.reservation.model.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.model.entity.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> getAll();

    Optional<ReservationTime> findById(Long id);

    ReservationTime getById(Long id);

    void remove(ReservationTime reservationTime);

    List<ReservationTime> getAllByThemeIdAndDate(Long themeId, LocalDate date);
}
