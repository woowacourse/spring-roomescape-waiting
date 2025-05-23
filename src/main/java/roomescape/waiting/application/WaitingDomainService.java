package roomescape.waiting.application;

import org.springframework.stereotype.Service;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.repository.WaitingRepository;

@Service
public class WaitingDomainService {

    private final WaitingRepository waitingRepository;

    public WaitingDomainService(final WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public Waiting save(final Waiting waiting) {
        return waitingRepository.save(waiting);
    }
}
