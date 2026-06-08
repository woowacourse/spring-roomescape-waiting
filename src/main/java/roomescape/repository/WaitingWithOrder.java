package roomescape.repository;

import roomescape.domain.Waiting;

public record WaitingWithOrder(
        Waiting waiting,
        Long waitingOrder
) {

    public static WaitingWithOrder of(
            Waiting waiting,
            Long waitingOrder
    ) {
        return new WaitingWithOrder(
                waiting,
                waitingOrder
        );
    }
}
