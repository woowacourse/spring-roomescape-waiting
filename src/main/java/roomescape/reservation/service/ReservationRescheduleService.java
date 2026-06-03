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
        Reservations slotOfReservations = findReservationsWithSlotLocked(slot);
        slotOfReservations.findPromoteWaiting()
                .ifPresent(this::promote);
    }

    private void promote(Reservation reservation) {
        reservation.promote();
        reservationRepository.updateStatus(reservation);
    }

    private Reservations findReservationsWithSlotLocked(ReservationSlot slot) {
        return new Reservations(reservationRepository.findReservedAndWaitingBySlot(slot));
    }

}
