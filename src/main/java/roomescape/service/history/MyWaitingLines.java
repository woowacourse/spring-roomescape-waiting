package roomescape.service.history;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.domain.reservationwaiting.ReservationWaitingLine.ReservationWaitingOrder;
import roomescape.repository.history.MyWaitingOrder;

public class MyWaitingLines {

    private final Map<Long, List<ReservationWaitingOrder>> waitingOrdersByReservationId;

    private MyWaitingLines(final Map<Long, List<ReservationWaitingOrder>> waitingOrdersByReservationId) {
        this.waitingOrdersByReservationId = waitingOrdersByReservationId;
    }

    public static MyWaitingLines from(final List<MyWaitingOrder> waitingOrders) {
        Map<Long, List<ReservationWaitingOrder>> waitingOrdersByReservationId = waitingOrders.stream()
                .collect(Collectors.groupingBy(
                        MyWaitingOrder::reservationId,
                        Collectors.mapping(MyWaitingLines::toWaitingOrder, Collectors.toList())
                ));

        return new MyWaitingLines(waitingOrdersByReservationId);
    }

    public int sequenceOf(final long reservationId, final long waitingId) {
        return ReservationWaitingLine.indexOf(
                        waitingOrdersByReservationId.getOrDefault(reservationId, List.of()),
                        waitingId
                )
                .orElseThrow(() -> new IllegalArgumentException("대기 순번을 찾을 수 없습니다."))
                + 1;
    }

    private static ReservationWaitingOrder toWaitingOrder(final MyWaitingOrder waitingOrder) {
        return new ReservationWaitingOrder(
                waitingOrder.waitingId(),
                waitingOrder.slotId(),
                waitingOrder.requestedAt()
        );
    }
}
