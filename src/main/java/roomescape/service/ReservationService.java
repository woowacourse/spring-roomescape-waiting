package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
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
import roomescape.exception.PastReservationControlException;
import roomescape.exception.PastTimeException;
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
        validDateTime(slot.getDate(), slot.getTimeSlot().getStartAt());
        checkDuplicateForSave(slot);
        return reservationRepository.save(Reservation.transientOf(request.name(), slot));
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
        validDateTime(newSlot.getDate(), newSlot.getTimeSlot().getStartAt());
        checkDuplicateForUpdate(newSlot, id);
        Reservation updated = reservationRepository.update(existing.reschedule(newSlot));
        processOldSlotIfChanged(existing.getSlot(), newSlot);
        return updated;
    }

    @Transactional
    public Reservation patchReservation(long id, String userName, ReservationPatchRequest request) {
        Reservation existing = validModifiable(id, userName);
        Slot targetSlot = slotService.findSlotOrNull(request.date(), request.timeId(), request.themeId());
        Slot persistentSlot = slotService.resolveSlot(targetSlot);
        validDateTime(persistentSlot.getDate(), persistentSlot.getTimeSlot().getStartAt());
        checkDuplicateForUpdate(persistentSlot, id);
        Reservation updated = reservationRepository.update(existing.reschedule(persistentSlot));
        processOldSlotIfChanged(existing.getSlot(), persistentSlot);
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
        existing.validateModifiable(userName);
        validUpcoming(existing.getSlot());
        return existing;
    }

    private void validUpcoming(Slot slot) {
        if (slot.getDate().isBefore(LocalDate.now()) || (slot.getDate().isEqual(LocalDate.now()) && slot.getTimeSlot()
                .getStartAt().isBefore(LocalTime.now()))) {
            throw new PastReservationControlException();
        }
    }

    private void validDateTime(LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now()) || (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now()))) {
            throw new PastTimeException("지난 시간/날짜로 예약하실 수 없습니다.");
        }
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
