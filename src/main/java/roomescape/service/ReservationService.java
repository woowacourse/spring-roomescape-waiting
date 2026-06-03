package roomescape.service;

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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final SlotService slotService;

    public ReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository, SlotService slotService) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.slotService = slotService;
    }

    @Transactional
    public Reservation saveReservation(ReservationRequest request) {
        Slot slot = slotService.resolveSlot(request.date(), request.timeId(), request.themeId());
        validDateTime(slot.getDate(), slot.getTimeSlot().getStartAt());
        validDuplicatedReservation(slot);

        Reservation transientReservation = Reservation.transientOf(request.name(), slot);
        return reservationRepository.save(transientReservation);
    }

    public List<Reservation> allReservations() {
        return reservationRepository.findAll();
    }

    public Reservation findReservationById(long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }

    public List<Booking> findReservationByName(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        List<Waiting> waitings = waitingRepository.findByName(name);
        List<Booking> bookings = new ArrayList<>();
        reservations.forEach(r -> bookings.add(Booking.fromReservation(r)));
        waitings.forEach(w -> bookings.add(Booking.fromWaiting(w)));
        return bookings;
    }

    @Transactional
    public void removeReservation(long reservationId, String userName) {
        Reservation reservation = validModifiable(reservationId, userName);
        reservationRepository.deleteById(reservationId);
        processSlotAfterCancellation(reservation.getSlot());
    }

    @Transactional
    public Reservation putReservation(long id, String userName, ReservationRequest request) {
        validModifiable(id, userName);
        Slot newSlot = slotService.resolveSlot(request.date(), request.timeId(), request.themeId());
        validDateTime(newSlot.getDate(), newSlot.getTimeSlot().getStartAt());
        validDuplicatedReservation(newSlot);

        Reservation reservation = new Reservation(id, request.name(), newSlot);
        return reservationRepository.update(reservation);
    }

    @Transactional
    public Reservation patchReservation(long id, String userName, ReservationPatchRequest request) {
        Reservation reservation = validModifiable(id, userName);
        Slot currentSlot = reservation.getSlot();

        LocalDate patchedDate = request.date() != null ? request.date() : currentSlot.getDate();
        Long patchedTimeId = request.timeId() != null ? request.timeId() : currentSlot.getTimeSlot().getId();
        Long patchedThemeId = request.themeId() != null ? request.themeId() : currentSlot.getTheme().getId();
        String patchedName = request.name() != null ? request.name() : reservation.getName();

        Slot newSlot = slotService.resolveSlot(patchedDate, patchedTimeId, patchedThemeId);
        validDateTime(newSlot.getDate(), newSlot.getTimeSlot().getStartAt());
        validDuplicatedReservation(newSlot);

        Reservation patchedReservation = new Reservation(id, patchedName, newSlot);
        return reservationRepository.update(patchedReservation);
    }

    private void processSlotAfterCancellation(Slot slot) {
        if (waitingRepository.isExistsBySlotId(slot.getId())) {
            Waiting firstWaiting = waitingRepository.findFirstBySlotId(slot.getId());
            waitingRepository.deleteById(firstWaiting.getId());
            reservationRepository.save(Reservation.transientOf(firstWaiting.getName(), slot));
        }
    }

    private Reservation validModifiable(long id, String userName) {
        Reservation existingReservation = findReservationById(id);
        existingReservation.validateModifiable(userName);
        validUpcoming(existingReservation.getSlot());
        return existingReservation;
    }

    private void validUpcoming(Slot slot) {
        if (slot.getDate().isBefore(LocalDate.now()) || (slot.getDate().isEqual(LocalDate.now()) && slot.getTimeSlot().getStartAt().isBefore(LocalTime.now()))) {
            throw new PastReservationControlException();
        }
    }

    private void validDateTime(LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now()) || (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now()))) {
            throw new PastTimeException("지난 시간/날짜로 예약하실 수 없습니다.");
        }
    }

    private void validDuplicatedReservation(Slot slot) {
        reservationRepository.findByDateAndTimeIdAndThemeId(slot.getDate(), slot.getTimeSlot().getId(), slot.getTheme().getId())
                .ifPresent(existing -> {
                    throw new DuplicateReservationException(existing.getSlot().getDate().toString(), existing.getSlot().getTimeSlot().getId(), existing.getSlot().getTheme().getId());
                });
    }
}
