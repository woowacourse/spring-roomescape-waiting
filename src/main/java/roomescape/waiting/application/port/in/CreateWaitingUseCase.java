package roomescape.waiting.application.port.in;

import roomescape.waiting.application.dto.request.WaitingRequest;
import roomescape.waiting.application.dto.response.WaitingResponse;

public interface CreateWaitingUseCase {
    WaitingResponse save(WaitingRequest body, long memberId);
}
