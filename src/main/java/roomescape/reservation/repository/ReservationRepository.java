package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.ReservationWithWaitingOrder;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    Reservation save(Reservation reservation);

    boolean update(Long id, Long timeId, LocalDateTime now);

    List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date);

    boolean deleteById(Long id);

    boolean hasConfirmedReservation(Long themeId, ReservationTime time);

    boolean existsByTimeId(Long timeId);

    Optional<Long> findEarliestWaiting(Long timeId, Long themeId);

    boolean promoteToReserved(Long waitingId);

    List<ReservationWithWaitingOrder> findAllByName(String name);

    boolean isDuplicatedWithName(String name, Long themeId, ReservationTime time);
}
