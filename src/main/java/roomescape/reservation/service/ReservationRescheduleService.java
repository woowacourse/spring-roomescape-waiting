package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Reservations;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.slot.domain.ReservationSlot;

@Service
@RequiredArgsConstructor
public class ReservationRescheduleService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public void rescheduleWaitingOrder(ReservationSlot slot) {
        Reservations slotOfReservations = findReservationsOfSlot(slot);
        slotOfReservations.promoteWaiting()
                .ifPresent(reservationRepository::updateStatus);
    }

    private Reservations findReservationsOfSlot(ReservationSlot slot) {
        return new Reservations(reservationRepository.findReservedAndWaitingBySlot(slot));
    }

}
