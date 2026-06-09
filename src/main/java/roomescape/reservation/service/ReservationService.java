package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Reservations;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithSlotInformation;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.exception.ReservationSlotException;
import roomescape.slot.repository.ReservationSlotRepository;

import java.util.List;

import static roomescape.slot.exception.ReservationSlotErrorInformation.SLOT_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    public List<ReservationWithSlotInformation> readAll() {
        return reservationRepository.findAll();
    }

    public List<ReservationWithSlotInformation> readAllByName(String name) {
        return reservationRepository.findByMemberName(name);
    }

    @Transactional
    public Reservation reserve(String requesterName, Long slotId) {
        ReservationSlot slot = getSlotAndReservationsWithLock(slotId);
        Reservation reservation = slot.reserve(requesterName);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancel(Long slotId, Long reservationId, String requesterName) {
        ReservationSlot slot = getSlotAndReservationsWithLock(slotId);
        Reservations changed = slot.cancelV2(reservationId, requesterName);
        cancelAndPromote(changed);
        return changed.findByName(requesterName);
    }

    @Transactional
    public Reservation cancelByManager(Long slotId, Long reservationId) {
        ReservationSlot slot = getSlotAndReservationsWithLock(slotId);
        Reservations changed = slot.cancelByManagerV2(reservationId);
        cancelAndPromote(changed);
        return changed.findById(reservationId);
    }

    @Transactional
    public Reservation reschedule(Long currentSlotId, Long newSlotId, Long reservationId, String requesterName) {
        ReservationSlot currentSlot = getSlotAndReservationsWithLock(currentSlotId);
        ReservationSlot newSlot = getSlotAndReservationsWithLock(newSlotId);

        Reservations changed = currentSlot.rescheduleV2(newSlot, reservationId, requesterName);
        rescheduleAndPromote(changed);
        return changed.findById(reservationId);
    }

    @Transactional
    public Reservation rescheduleByManager(Long currentSlotId, Long newSlotId, Long reservationId) {
        ReservationSlot currentSlot = getSlotAndReservationsWithLock(currentSlotId);
        ReservationSlot newSlot = getSlotAndReservationsWithLock(newSlotId);

        Reservations changed = currentSlot.rescheduleByManagerV2(newSlot, reservationId);
        rescheduleAndPromote(changed);
        return changed.findById(reservationId);
    }

    private ReservationSlot getSlotAndReservationsWithLock(Long slotId) {
        ReservationSlot slot = getSlotWithLock(slotId);
        List<Reservation> activeReservations = getReservationsOfSlot(slot);
        return slot.withReservations(new Reservations(activeReservations));
    }

    private ReservationSlot getSlotWithLock(Long slotId) {
        return reservationSlotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new ReservationSlotException(SLOT_NOT_FOUND));
    }

    private List<Reservation> getReservationsOfSlot(ReservationSlot slot) {
        return reservationRepository.findReservedAndWaitingBySlotId(slot.getId());
    }

    public void cancelAndPromote(Reservations changed) {
        changed.values().forEach(reservationRepository::updateStatus);
    }

    private void rescheduleAndPromote(Reservations changed) {
        changed.values().forEach(reservationRepository::updateSchedule);
    }

}
