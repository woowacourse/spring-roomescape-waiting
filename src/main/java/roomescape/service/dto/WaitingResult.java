package roomescape.service.dto;

import roomescape.domain.Waiting;

public record WaitingResult(
        Waiting waiting,
        Long order
) {

    public static WaitingResult of(Waiting waiting, Long order) {
        return new WaitingResult(waiting, order);
    }
}
