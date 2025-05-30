package roomescape.reservation.model.entity.vo;

import java.util.Set;
import roomescape.reservation.model.exception.ReservationException.InvalidStatusTransitionException;

public enum ReservationWaitingStatus {
    DENIED(Set.of()),
    CANCELED(Set.of()),
    ACCEPTED(Set.of(CANCELED)),
    PENDING(Set.of(ACCEPTED, DENIED, CANCELED));

    private final Set<ReservationWaitingStatus> allowedTransitions;

    ReservationWaitingStatus(Set<ReservationWaitingStatus> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public void validateTransition(ReservationWaitingStatus newStatus) {
        if (!allowedTransitions.contains(newStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("%s 상태에서 %s로 변경할 수 없습니다.", this, newStatus)
            );
        }
    }
}
