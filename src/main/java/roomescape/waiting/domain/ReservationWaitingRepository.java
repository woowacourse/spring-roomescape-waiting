package roomescape.waiting.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    Optional<ReservationWaiting> findById(Long id);

    List<ReservationWaiting> findAllByName(String name);

    boolean existsByDateAndTimeIdAndName(LocalDate date, Long timeId, String name);

    int deleteById(Long id);

    List<ReservationWaiting> findAllByDateAndTimeIdAndThemeIdForUpdate(LocalDate date, Long timeId, Long themeId);
}
