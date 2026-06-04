package roomescape.waiting.application;

import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;

public interface WaitingReference {
    void validateExistReservation(WaitingCreateCommand waitingCreateCommand);
    void promoteToReservation(Waiting waiting);
}
