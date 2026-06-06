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

import java.time.LocalDateTime;
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
        Reservation reservation = slot.reserve(requesterName, LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelByManager(Long slotId, Long reservationId) {
        ReservationSlot slot = getSlotAndReservationsWithLock(slotId);
        Reservations changed = slot.cancelByManager(reservationId, LocalDateTime.now());
        cancelAndPromote(changed);
        return changed.findById(reservationId);
    }

    @Transactional
    public Reservation cancel(Long slotId, String requesterName) {
        ReservationSlot slot = getSlotAndReservationsWithLock(slotId);
        Reservations changed = slot.cancel(requesterName, LocalDateTime.now());
        cancelAndPromote(changed);
        return changed.findByName(requesterName);
    }

    @Transactional
    public Reservation reschedule(Long currentSlotId, Long newSlotId, String requesterName) {
        ReservationSlot currentSlot = getSlotAndReservationsWithLock(currentSlotId);
        ReservationSlot newSlot = getSlotAndReservationsWithLock(newSlotId);

        Reservations changed = currentSlot.reschedule(newSlot, requesterName, LocalDateTime.now());
        rescheduleAndPromote(changed);
        return changed.findByName(requesterName);
    }

    @Transactional
    public Reservation rescheduleByManager(Long currentSlotId, Long newSlotId, String requesterName) {
        ReservationSlot currentSlot = getSlotAndReservationsWithLock(currentSlotId);
        ReservationSlot newSlot = getSlotAndReservationsWithLock(newSlotId);

        Reservations changed = currentSlot.rescheduleByManager(newSlot, requesterName, LocalDateTime.now());
        rescheduleAndPromote(changed);
        return changed.findByName(requesterName);
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

    private void cancelAndPromote(Reservations changed) {
        changed.values().forEach(reservationRepository::updateStatus);
    }

    private void rescheduleAndPromote(Reservations changed) {
        changed.values().forEach(reservationRepository::updateSchedule);
    }

}
