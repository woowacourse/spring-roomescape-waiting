package roomescape.repository.history;

import java.util.List;

public interface MyHistoryRepository {
    List<MyHistory> findByUserName(String name);

    List<MyWaitingOrder> findWaitingOrdersByReservationIds(List<Long> reservationIds);
}
