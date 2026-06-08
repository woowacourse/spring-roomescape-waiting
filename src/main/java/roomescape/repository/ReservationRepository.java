package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.ReservationWithWaitingOrder;

public interface ReservationRepository {
    List<ReservationWithWaitingOrder> findAll();

    List<ReservationWithWaitingOrder> findByReserverName(String reserverName);

    Optional<Reservation> findById(Long id);

    ReservationWithWaitingOrder save(Reservation reservation);

    ReservationWithWaitingOrder update(Reservation reservation);

    void cancel(Long id);

    Optional<Theme> lockTheme(Long themeId);

    boolean promoteEarliestWaiting(LocalDate date, Long timeId, Long themeId);

    boolean existsActiveConfirmed(LocalDate date, Long timeId, Long themeId);

    boolean existsById(Long id);

    boolean existsByReserverNameAndDateAndTimeIdAndThemeId(String reserverName, LocalDate date, Long timeId, Long themeId);

    boolean existsByReserverNameAndDateAndTimeIdAndThemeIdAndIdNot(
            String reserverName, LocalDate date, Long timeId, Long themeId, Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
