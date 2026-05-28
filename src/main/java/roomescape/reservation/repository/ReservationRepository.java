package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    List<Reservation> findAllByNameOrderByDateAndTime(String name);

    Optional<Reservation> findById(Long id);

    List<Reservation> findReservedAndWaitingBySlot(Long dateId, Long timeId, Long themeId);

    Reservation save(Reservation reservation);

    boolean existsByDateAndTimeAndThemeId(Long dateId, Long timeId, Long themeId);

    boolean existsByNameAndDateAndTime(String name, Long dateId, Long timeId);

    boolean existsByDateId(Long dateId);

    boolean existsByTimeId(Long timeId);

    boolean updateStatus(Reservation reservation);

    boolean updateSchedule(Reservation reservation);

    List<ReservationWithWaitingTurn> findMyReservationsWithWaitingTurn(String memberName);

}
