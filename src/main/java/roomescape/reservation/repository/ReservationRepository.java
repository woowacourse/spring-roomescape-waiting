package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findReservedAndWaitingBySlot(Long dateId, Long timeId, Long themeId);

    List<ReservationWithWaitingTurn> findMyReservationsWithWaitingTurn(String memberName);

    Reservation save(Reservation reservation);

    boolean existsReservedBySlot(Long dateId, Long timeId, Long themeId);

    boolean updateStatus(Reservation reservation);

    boolean updateSchedule(Reservation reservation);


}
