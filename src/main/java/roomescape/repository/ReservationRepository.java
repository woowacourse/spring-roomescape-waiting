package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.service.dto.ReservationWithWaitingOrder;

public interface ReservationRepository {
    List<ReservationWithWaitingOrder> findAll();

    List<ReservationWithWaitingOrder> findByReserverName(String reserverName);

    Optional<Reservation> findById(Long id);

    ReservationWithWaitingOrder save(Reservation reservation);

    ReservationWithWaitingOrder update(Reservation reservation);

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByReserverNameAndDateAndTimeIdAndThemeId(String reserverName, LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeIdAndThemeIdAndIdNot(LocalDate date, Long timeId, Long themeId, Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
