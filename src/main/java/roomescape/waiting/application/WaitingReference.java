package roomescape.waiting.application;

import roomescape.waiting.application.dto.WaitingCreateCommand;

public interface WaitingReference {
    void validateExistReservation(WaitingCreateCommand waitingCreateCommand);
}
