package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWithWaitingOrder;

public interface ReservationRepository {
    List<ReservationWithWaitingOrder> findAllActive();

    List<ReservationWithWaitingOrder> findByReserverName(String reserverName);

    Optional<Reservation> findById(Long id);

    Optional<ReservationWithWaitingOrder> findWithWaitingOrderById(Long id);

    <T> T executeWithThemeLock(Long themeId, ThemeLockedAction<T> action);

    boolean existsActiveConfirmed(LocalDate date, Long timeId, Long themeId);

    boolean existsById(Long id);

    boolean existsByReserverNameAndDateAndTimeIdAndThemeId(String reserverName, LocalDate date, Long timeId, Long themeId);

    boolean existsByReserverNameAndDateAndTimeIdAndThemeIdAndIdNot(
            String reserverName, LocalDate date, Long timeId, Long themeId, Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
