package roomescape.reservationWaiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservationWaiting.domain.ReservationWaiting;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    Optional<ReservationWaiting> findById(Long id);

    List<ReservationWaiting> findAllByName(String name);

    boolean existsByDateAndTimeIdAndThemeIdAndName(LocalDate date, Long timeId, Long themeId, String name);

    int deleteById(Long id);

    Optional<ReservationWaiting> findFirstByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
