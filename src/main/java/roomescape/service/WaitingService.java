package roomescape.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateWaitingException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    public int waitingNumber(Waiting waiting) {
        return waitingRepository.calculateWaitingNumber(waiting);
    }

    @Transactional
    public void saveWaiting(Waiting waiting) {
        validDuplicated(waiting);
        validReservation(waiting);
        waitingRepository.save(waiting);
    }

    @Transactional
    public void removeWaiting(Waiting waiting) {
        existsWaiting(waiting);
        waitingRepository.delete(waiting);
    }

    public Integer allWaitingOf(LocalDate date, Long timeSlotId, Long themeId) {
        return waitingRepository.countAllBy(date, timeSlotId, themeId);
    }

    private void existsWaiting(Waiting waiting) {
        int waitingNumber = waitingNumber(waiting);
        if (waitingNumber < 1) {
            throw new WaitingNotFoundException(waiting);
        }
    }

    private void validReservation(Waiting waiting) {
        reservationRepository.findByDateAndTimeIdAndThemeId(waiting.getDate(),
                waiting.getTimeSlotId(), waiting.getThemeId()).ifPresent(reservation -> {
            if (reservation.getName().equals(waiting.getName())) {
                throw new DuplicateReservationException(reservation.getDate().toString(),
                        reservation.getTimeSlot().getId(), reservation.getTheme().getId());
            }
        });
    }

    private void validDuplicated(Waiting waiting) {
        boolean isExists = waitingRepository.isExists(waiting);
        if (isExists) {
            throw new DuplicateWaitingException(waiting);
        }
    }
}
