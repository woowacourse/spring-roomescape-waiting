package roomescape.waiting.application.port.in;

import java.util.List;
import roomescape.waiting.application.dto.response.WaitingDetailFindResponse;

public interface FindWaitingUseCase {
    List<WaitingDetailFindResponse> findWaitingDetails();
}
