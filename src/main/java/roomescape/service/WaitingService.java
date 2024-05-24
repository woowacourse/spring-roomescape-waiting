package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;

    public WaitingService(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }


}
