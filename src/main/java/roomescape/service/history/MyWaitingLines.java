package roomescape.service.history;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.repository.history.MyWaitingOrder;

public class MyWaitingLines {

    private final Map<Long, ReservationWaitingLine> waitingLinesByReservationId;

    private MyWaitingLines(final Map<Long, ReservationWaitingLine> waitingLinesByReservationId) {
        this.waitingLinesByReservationId = waitingLinesByReservationId;
    }

    public static MyWaitingLines from(final List<MyWaitingOrder> waitingOrders) {
        Map<Long, ReservationWaitingLine> waitingLinesByReservationId = waitingOrders.stream()
                .collect(Collectors.groupingBy(
                        MyWaitingOrder::reservationId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                MyWaitingLines::toWaitingLine
                        )
                ));

        return new MyWaitingLines(waitingLinesByReservationId);
    }

    public int sequenceOf(final long reservationId, final long waitingId) {
        return waitingLinesByReservationId.get(reservationId)
                .sequenceOf(waitingId);
    }

    private static ReservationWaitingLine toWaitingLine(final List<MyWaitingOrder> waitingOrders) {
        return new ReservationWaitingLine(waitingOrders.stream()
                .map(order -> new ReservationWaitingLine.ReservationWaitingOrder(
                        order.waitingId(),
                        order.requestedAt()
                ))
                .toList());
    }
}
