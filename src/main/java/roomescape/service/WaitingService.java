package roomescape.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateWaitingException;
import roomescape.exception.InvalidWaitingPrerequisiteException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final SlotService slotService;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository,
                          SlotService slotService) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.slotService = slotService;
    }

    @Transactional
    public Waiting saveWaiting(WaitingRequest request) {
        Slot slot = slotService.findSlotOrNull(request.date(), request.timeId(), request.themeId());
        validPrerequisite(slot);
        Reservation reservation = findReservationOrThrow(slot);
        validNotReservedBySelf(reservation, request.name());

        Waiting waiting = Waiting.transientOf(request.name(), slot);
        validDuplicated(waiting);
        waiting.validateNotPast(LocalDateTime.now());

        return waitingRepository.save(waiting);
    }

    @Transactional
    public void removeWaiting(Long id, String userName) {
        Waiting waiting = waitingRepository.findById(id).orElseThrow(() -> new WaitingNotFoundException(id));
        waiting.validateModifiable(userName, LocalDateTime.now());
        waitingRepository.deleteById(id);
    }

    private void validPrerequisite(Slot slot) {
        if (slot == null) {
            throw new InvalidWaitingPrerequisiteException();
        }
    }

    private Reservation findReservationOrThrow(Slot slot) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(slot.getDate(), slot.getTimeSlot().getId(),
                        slot.getTheme().getId())
                .orElseThrow(InvalidWaitingPrerequisiteException::new);
    }

    private void validNotReservedBySelf(Reservation reservation, String userName) {
        if (reservation.getName().equals(userName)) {
            throw new DuplicateReservationException(
                    reservation.getSlot().getDate().toString(),
                    reservation.getSlot().getTimeSlot().getId(),
                    reservation.getSlot().getTheme().getId()
            );
        }
    }

    private void validDuplicated(Waiting waiting) {
        if (waitingRepository.isExists(waiting)) {
            throw new DuplicateWaitingException(waiting);
        }
    }
}
