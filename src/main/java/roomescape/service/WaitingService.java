package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateWaitingException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;

    public WaitingService(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public int waitingNumber(Waiting waiting) {
        return waitingRepository.calculateWaitingNumber(waiting);
    }

    @Transactional
    public void saveWaiting(Waiting waiting) {
        validDuplicatedReservation(waiting);
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

    public List<Waiting> findWaitingByName(String name) {
        return waitingRepository.findByName(name);
    }

    private void existsWaiting(Waiting waiting) {
        int waitingNumber = waitingNumber(waiting);
        if (waitingNumber < 1) {
            throw new WaitingNotFoundException(waiting);
        }
    }

    private void validDuplicatedReservation(Waiting waiting) {
        boolean isExists = waitingRepository.isExists(waiting);
        if (isExists) {
            throw new DuplicateWaitingException(waiting);
        }
    }
}
