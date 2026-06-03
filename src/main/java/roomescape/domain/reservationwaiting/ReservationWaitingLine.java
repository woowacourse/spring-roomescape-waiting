package roomescape.domain.reservationwaiting;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import roomescape.domain.reservation.ReservationName;

public class ReservationWaitingLine {
    private final Map<Long, Integer> sequencesByWaitingId;
    private final Set<String> names;

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
        this.names = sortedOrders.stream()
                .map(ReservationWaitingOrder::name)
                .filter(Objects::nonNull)
                .map(name -> ReservationName.from(name).value())
                .collect(Collectors.toSet());
    }

    public int sequenceOf(final long waitingId) {
        for (int index = 0; index < orders.size(); index++) {
            if (orders.get(index).waitingId() == waitingId) {
                return index + 1;
            }
        }

        return sequence;
    }

    public record ReservationWaitingOrder(
            long waitingId,
            LocalDateTime requestedAt
    ) {
        public ReservationWaitingOrder(final long waitingId, final LocalDateTime requestedAt) {
            this(waitingId, requestedAt, null);
        }
    }
}
