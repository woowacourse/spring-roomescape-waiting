package roomescape.reservation.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ReservationWaitings {

    private final List<Reservation> waitings;

    public ReservationWaitings(List<Reservation> waitings) {
        validateAllWaiting(waitings);
        this.waitings = List.copyOf(waitings);
    }

    private void validateAllWaiting(List<Reservation> waitings) {
        boolean hasNoWaiting = waitings.stream()
                .anyMatch(r -> r.getStatus() != Status.WAITING);
        if (hasNoWaiting) {
            throw new IllegalArgumentException("모든 예약은 WAITING이어야 합니다.");
        }
    }

    public Optional<Reservation> earliest() {
        return waitings.stream()
                .min(Comparator.comparing(Reservation::getCreatedAt));
    }

    public int order(Long id) {
        return waitings.stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt))
                .map(Reservation::getId)
                .toList()
                .indexOf(id) + 1;
    }
}
