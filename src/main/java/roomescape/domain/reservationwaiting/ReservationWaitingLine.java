package roomescape.domain.reservationwaiting;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import roomescape.domain.reservation.ReservationName;

public class ReservationWaitingLine {
    private final List<ReservationWaitingOrder> orders;

    public ReservationWaitingLine(final List<ReservationWaitingOrder> orders) {
        this.orders = orders.stream()
                .sorted(Comparator.comparing(ReservationWaitingOrder::requestedAt)
                        .thenComparing(ReservationWaitingOrder::waitingId))
                .toList();
    }

    public static ReservationWaitingLine fromWaitings(final List<ReservationWaiting> waitings) {
        return new ReservationWaitingLine(waitings.stream()
                .map(waiting -> new ReservationWaitingOrder(
                        waiting.getId(),
                        waiting.getRequestedAt(),
                        waiting.getName()
                ))
                .toList());
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    public boolean containsName(final ReservationName name) {
        return orders.stream()
                .map(ReservationWaitingOrder::name)
                .filter(Objects::nonNull)
                .map(waitingName -> ReservationName.from(waitingName).value())
                .anyMatch(name.value()::equals);
    }

    public int sequenceOf(final long waitingId) {
        for (int index = 0; index < orders.size(); index++) {
            if (orders.get(index).waitingId() == waitingId) {
                return index + 1;
            }
        }

        throw new IllegalArgumentException("대기 순번을 찾을 수 없습니다.");
    }

    public record ReservationWaitingOrder(
            long waitingId,
            LocalDateTime requestedAt,
            String name
    ) {
        public ReservationWaitingOrder(final long waitingId, final LocalDateTime requestedAt) {
            this(waitingId, requestedAt, null);
        }
    }
}
