package roomescape.domain.reservationwaiting;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class ReservationWaitingLine {
    private final List<ReservationWaitingOrder> orders;

    public ReservationWaitingLine(final List<ReservationWaitingOrder> orders) {
        this.orders = orders.stream()
                .sorted(Comparator.comparing(ReservationWaitingOrder::requestedAt)
                        .thenComparing(ReservationWaitingOrder::waitingId))
                .toList();
    }

    public int sequenceOf(final Long waitingId) {
        for (int index = 0; index < orders.size(); index++) {
            if (orders.get(index).waitingId().equals(waitingId)) {
                return index + 1;
            }
        }

        throw new IllegalArgumentException("대기 순번을 찾을 수 없습니다.");
    }

    public record ReservationWaitingOrder(
            Long waitingId,
            LocalDateTime requestedAt
    ) {
    }
}
