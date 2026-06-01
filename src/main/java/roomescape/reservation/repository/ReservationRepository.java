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

    Reservation update(Reservation reservation);

    List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date);

    boolean deleteById(Long id);

    boolean isDuplicated(Long themeId, ReservationTime time);

    boolean existsByTimeId(Long timeId);

    Optional<Long> findEarliestWaiting(Long timeId, Long themeId);

    void promoteToReserved(Long waitingId);

    List<ReservationWithWaitingOrder> findAllByName(String name);

    List<Reservation> findByName(String name);

    boolean isDuplicatedWithName(String name, Long themeId, ReservationTime time);

    List<Reservation> findAllWaitingBy(Long timeId, Long themeId);
}
