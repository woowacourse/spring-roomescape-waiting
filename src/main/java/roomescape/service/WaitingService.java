package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.WaitingNotFoundException;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.WaitingCommand;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;

    public WaitingService(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public int waitingNumber(WaitingCommand waiting) {
        return waitingRepository.calculateWaitingNumber(waiting);
    }

    @Transactional
    public void saveWaiting(WaitingCommand waiting) {
        validDuplicatedReservation(waiting);
        waitingRepository.save(waiting);
    }

    @Transactional
    public void removeWaiting(WaitingCommand waiting) {
        existsWaiting(waiting);
        waitingRepository.delete(waiting);
    }

    private void existsWaiting(WaitingCommand waiting) {
        int waitingNumber = waitingNumber(waiting);
        if (waitingNumber < 1) {
            throw new WaitingNotFoundException(waiting);
        }
    }

    private void validDuplicatedReservation(WaitingCommand waiting) {
    }
}
