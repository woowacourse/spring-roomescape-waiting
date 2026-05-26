package roomescape.domain.waiting;

import org.springframework.stereotype.Service;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;

    public WaitingService(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }
}
