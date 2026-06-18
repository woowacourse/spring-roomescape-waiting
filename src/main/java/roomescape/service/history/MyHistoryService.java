package roomescape.service.history;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.repository.history.MyHistory;
import roomescape.repository.history.MyHistoryRepository;
import roomescape.repository.history.MyWaitingOrder;

@Service
public class MyHistoryService {

    private final MyHistoryRepository myHistoryRepository;

    public MyHistoryService(final MyHistoryRepository myHistoryRepository) {
        this.myHistoryRepository = myHistoryRepository;
    }

    public List<MyHistoryResult> getAllByName(final String name) {
        MyHistories histories = new MyHistories(myHistoryRepository.findByUserName(name));

        List<MyWaitingOrder> waitingOrders = myHistoryRepository.findWaitingOrdersByReservationIds(
                histories.waitingReservationIds()
        );

        return histories.toResults(MyWaitingLines.from(waitingOrders));
    }
}
