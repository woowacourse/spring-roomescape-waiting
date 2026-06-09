package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.ReservationWithSlotInformation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    List<ReservationWithSlotInformation> findAll();

    List<ReservationWithSlotInformation> findByMemberName(String name);

    List<Reservation> findReservedAndWaitingBySlotId(Long slotId);

    Reservation save(Reservation reservation);

    boolean updateStatus(Reservation reservation);

    boolean updateSchedule(Reservation reservation);

}
