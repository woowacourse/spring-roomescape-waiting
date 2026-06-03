package roomescape.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.waiting.domain.ReservationWaiting;

public interface ReservationWaitingDao {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    Optional<ReservationWaiting> findById(Long id);

    List<ReservationWaiting> findAllByName(String name);

    boolean existsByDateAndTimeIdAndName(LocalDate date, Long timeId, String name);

    void delete(ReservationWaiting reservationWaiting);

    List<ReservationWaiting> findAllByDateAndTimeIdAndThemeIdForUpdate(LocalDate date, Long timeId, Long themeId);
}
