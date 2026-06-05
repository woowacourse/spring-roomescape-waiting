package roomescape.reservationwaiting.repository;

import java.time.LocalDate;
import java.util.Optional;
import roomescape.reservationwaiting.ReservationWaiting;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);
    int deleteById(Long id);
    int deleteByIdAndName(Long id, String name);
    boolean existsByDateAndThemeIdAndTimeIdAndName(LocalDate date, Long themeId, Long timeId, String name);
    Optional<ReservationWaiting> findFirstWaiting(LocalDate date, Long themeId, Long timeId);
}
