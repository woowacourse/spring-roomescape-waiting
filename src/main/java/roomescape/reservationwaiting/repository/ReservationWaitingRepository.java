package roomescape.reservationwaiting.repository;

import java.time.LocalDate;
import roomescape.reservationwaiting.ReservationWaiting;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);
    int deleteByIdAndName(Long id, String name);
    boolean existsByDateAndThemeIdAndTimeIdAndName(LocalDate date, Long themeId, Long timeId, String name);
}
