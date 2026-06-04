package roomescape.domain.reservationwaiting;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReservationWaitingLine {
    private final Map<Long, Integer> sequencesByWaitingId;

    public ReservationWaitingLine(final List<ReservationWaitingOrder> orders) {
        List<ReservationWaitingOrder> sortedOrders = orders.stream()
                .sorted(Comparator.comparing(ReservationWaitingOrder::requestedAt)
                        .thenComparing(ReservationWaitingOrder::waitingId))
                .toList();

        this.sequencesByWaitingId = IntStream.range(0, sortedOrders.size())
                .boxed()
                .collect(Collectors.toMap(
                        index -> sortedOrders.get(index).waitingId(),
                        index -> index + 1
                ));
    }

    public int sequenceOf(final long waitingId) {
        Integer sequence = sequencesByWaitingId.get(waitingId);

        if (sequence == null) {
            throw new IllegalArgumentException("대기 순번을 찾을 수 없습니다.");
        }

        return sequence;
    }

    public record ReservationWaitingOrder(
            long waitingId,
            LocalDateTime requestedAt
    ) {
    }
}
