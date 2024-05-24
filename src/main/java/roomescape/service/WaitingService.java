package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Waiting;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.input.WaitingInput;
import roomescape.service.dto.output.WaitingOutput;

@Service
public class WaitingService {

    private final CreateValidator createValidator;
    private final WaitingRepository waitingRepository;

    public WaitingService(WaitingRepository waitingRepository, CreateValidator createValidator) {
        this.waitingRepository = waitingRepository;
        this.createValidator = createValidator;
    }

    public WaitingOutput createWaiting(final WaitingInput input) {
        final Waiting waiting = createValidator.validateWaitingInput(input);
        final Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingOutput.toOutput(savedWaiting);
    }
}
