package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.slot.domain.ReservationSlot;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findReservedAndWaitingBySlot(ReservationSlot slot);

    List<ReservationWithWaitingTurn> findMyReservationsWithWaitingTurn(String memberName);

    Reservation save(Reservation reservation);

    boolean existsReservedBySlot(ReservationSlot slot);

    boolean updateStatus(Reservation reservation);

    boolean updateSchedule(Reservation reservation);


}
