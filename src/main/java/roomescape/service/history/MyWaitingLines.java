package roomescape.service.history;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import roomescape.repository.history.MyWaitingOrder;

public class MyWaitingLines {

    private final Map<Long, List<MyWaitingOrder>> waitingOrdersByReservationId;

    private MyWaitingLines(final Map<Long, List<MyWaitingOrder>> waitingOrdersByReservationId) {
        this.waitingOrdersByReservationId = waitingOrdersByReservationId;
    }

    public static MyWaitingLines from(final List<MyWaitingOrder> waitingOrders) {
        Map<Long, List<MyWaitingOrder>> waitingOrdersByReservationId = waitingOrders.stream()
                .collect(Collectors.groupingBy(
                        MyWaitingOrder::reservationId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                MyWaitingLines::sortByWaitingOrder
                        )
                ));

        return new MyWaitingLines(waitingOrdersByReservationId);
    }

    public int sequenceOf(final long reservationId, final long waitingId) {
        List<MyWaitingOrder> waitingOrders = waitingOrdersByReservationId.getOrDefault(reservationId, List.of());
        for (int index = 0; index < waitingOrders.size(); index++) {
            if (waitingOrders.get(index).waitingId() == waitingId) {
                return index + 1;
            }
        }

        throw new IllegalArgumentException("대기 순번을 찾을 수 없습니다.");
    }

    private static List<MyWaitingOrder> sortByWaitingOrder(final List<MyWaitingOrder> waitingOrders) {
        return waitingOrders.stream()
                .sorted(Comparator.comparing(MyWaitingOrder::requestedAt)
                        .thenComparing(MyWaitingOrder::waitingId))
                .toList();
    }
}
