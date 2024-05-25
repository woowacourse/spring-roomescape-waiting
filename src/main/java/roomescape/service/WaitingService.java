package roomescape.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.controller.api.dto.request.LoginMemberRequest;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.user.Role;
import roomescape.exception.UnauthorizedException;
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

    public List<WaitingOutput> getAllWaitings() {
        final List<Waiting> waitings = waitingRepository.findAll();
        return WaitingOutput.toOutputs(waitings);
    }

    public void deleteWaiting(long id, LoginMemberRequest loginMemberRequest) {
        Optional<Waiting> waiting = waitingRepository.findById(id);
        if (waiting.isPresent() &&
                waiting.get().getMember().getId() != loginMemberRequest.id() &&
                !Objects.equals(loginMemberRequest.role(), Role.ADMIN.getValue())) {
            throw new UnauthorizedException();
        }
        waitingRepository.deleteById(id);
    }
}
