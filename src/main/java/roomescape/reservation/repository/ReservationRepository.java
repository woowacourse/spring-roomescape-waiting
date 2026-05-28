package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Reservation> findReservedAndWaitingBySlot(Long dateId, Long timeId, Long themeId);

    Reservation save(Reservation reservation);

    boolean existsByDateAndTimeAndThemeId(Long dateId, Long timeId, Long themeId);

    boolean updateStatus(Reservation reservation);

    boolean updateSchedule(Reservation reservation);

    List<ReservationWithWaitingTurn> findMyReservationsWithWaitingTurn(String memberName);

}
