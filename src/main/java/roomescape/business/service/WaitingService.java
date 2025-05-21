package roomescape.business.service;

import roomescape.infrastructure.repository.WaitingRepository;

public class WaitingService {

    private final WaitingRepository waitingRepository;

    public WaitingService(final WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }
}
