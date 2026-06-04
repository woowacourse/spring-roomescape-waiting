package roomescape.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.ReservationPatchRequest;
import roomescape.controller.dto.ReservationRequest;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.Booking;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final SlotService slotService;

    public ReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository,
                              SlotService slotService) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.slotService = slotService;
    }

    @Transactional
    public Reservation saveReservation(ReservationRequest request) {
        Slot slot = slotService.resolveNewSlot(request.date(), request.timeId(), request.themeId());
        Reservation reservation = Reservation.transientOf(request.name(), slot);
        reservation.validateNotPast(LocalDateTime.now());
        checkDuplicateForSave(slot);
        return reservationRepository.save(reservation);
    }

    public List<Reservation> allReservations() {
        return reservationRepository.findAll();
    }

    public Reservation findReservationById(long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }

    public List<Booking> findReservationByName(String name) {
        List<Booking> bookings = new ArrayList<>();
        reservationRepository.findByName(name).forEach(r -> bookings.add(Booking.fromReservation(r)));
        waitingRepository.findByName(name).forEach(w -> bookings.add(Booking.fromWaiting(w)));
        return bookings;
    }

    @Transactional
    public void removeReservation(long id, String userName) {
        Reservation reservation = validModifiable(id, userName);
        reservationRepository.deleteById(id);
        processOldSlot(reservation.getSlot());
    }

    @Transactional
    public Reservation putReservation(long id, String userName, ReservationRequest request) {
        Reservation existing = validModifiable(id, userName);
        Slot newSlot = slotService.resolveNewSlot(request.date(), request.timeId(), request.themeId());
        return updateValidReservation(id, newSlot, existing);
    }

    @Transactional
    public Reservation patchReservation(long id, String userName, ReservationPatchRequest request) {
        Reservation existing = validModifiable(id, userName);
        Slot targetSlot = slotService.findSlotOrNull(request.date(), request.timeId(), request.themeId());
        Slot persistentSlot = slotService.resolveSlot(targetSlot);
        return updateValidReservation(id, persistentSlot, existing);
    }

    private Reservation updateValidReservation(long id, Slot slot, Reservation existing) {
        checkDuplicateForUpdate(slot, id);
        Reservation updated = reservationRepository.update(existing.reschedule(slot, LocalDateTime.now()));
        processOldSlotIfChanged(existing.getSlot(), slot);
        return updated;
    }

    private void processOldSlotIfChanged(Slot oldSlot, Slot newSlot) {
        if (!oldSlot.getId().equals(newSlot.getId())) {
            processOldSlot(oldSlot);
        }
    }

    private void processOldSlot(Slot slot) {
        if (waitingRepository.isExistsBySlotId(slot.getId())) {
            promoteFirstWaiting(slot);
            return;
        }
        slotService.deleteSlot(slot.getId());
    }

    private void promoteFirstWaiting(Slot slot) {
        Waiting firstWaiting = waitingRepository.findFirstBySlotId(slot.getId());
        waitingRepository.deleteById(firstWaiting.getId());
        reservationRepository.save(Reservation.transientOf(firstWaiting.getName(), slot));
    }

    private Reservation validModifiable(long id, String userName) {
        Reservation existing = findReservationById(id);
        existing.validateModifiable(userName, LocalDateTime.now());
        return existing;
    }

    private void checkDuplicateForSave(Slot slot) {
        if (reservationRepository.findByDateAndTimeIdAndThemeId(slot.getDate(), slot.getTimeSlot().getId(),
                slot.getTheme().getId()).isPresent()) {
            throw new DuplicateReservationException(slot.getDate().toString(), slot.getTimeSlot().getId(),
                    slot.getTheme().getId());
        }
    }

    private void checkDuplicateForUpdate(Slot slot, long targetId) {
        reservationRepository.findByDateAndTimeIdAndThemeId(slot.getDate(), slot.getTimeSlot().getId(),
                        slot.getTheme().getId())
                .filter(existing -> !existing.getId().equals(targetId))
                .ifPresent(existing -> {
                    throw new DuplicateReservationException(slot.getDate().toString(), slot.getTimeSlot().getId(),
                            slot.getTheme().getId());
                });
    }
}
