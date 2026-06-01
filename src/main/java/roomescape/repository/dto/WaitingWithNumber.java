package roomescape.repository.dto;

import roomescape.domain.Waiting;

public record WaitingWithNumber(
        Waiting waiting,
        Integer waitingNumber
) {
}
