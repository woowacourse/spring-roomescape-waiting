package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.ReservationWithWaitingOrder;
import roomescape.time.domain.ReservationTime;

public interface ReservationRepository {
    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Reservation> findByName(String name);

    Reservation save(Reservation reservation);

    boolean update(Long id, Long timeId);

    List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date);

    boolean deleteById(Long id);

    boolean isDuplicated(Long themeId, ReservationTime time);

    boolean existsByTimeId(Long timeId);

    Optional<Long> findEarliestWaiting(Long timeId, Long themeId);

    boolean promoteToReserved(Long waitingId);

    List<ReservationWithWaitingOrder> findAllByName(String name);
}
