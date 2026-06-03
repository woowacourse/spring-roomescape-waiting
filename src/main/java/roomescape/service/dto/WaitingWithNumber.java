package roomescape.service.dto;

import roomescape.domain.Waiting;

public record WaitingWithNumber(
        Waiting waiting,
        int waitingNumber
) {
}
