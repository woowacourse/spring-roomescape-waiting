package roomescape.service.history;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.repository.history.MyHistory;
import roomescape.repository.history.MyHistoryRepository;

@Service
public class MyHistoryService {

    private final MyHistoryRepository myHistoryRepository;

    public MyHistoryService(final MyHistoryRepository myHistoryRepository) {
        this.myHistoryRepository = myHistoryRepository;
    }

    public List<MyHistoryResult> getAllByName(final String name) {
        return myHistoryRepository.findByUserName(name).stream()
                .map(this::toResult)
                .toList();
    }

    private MyHistoryResult toResult(final MyHistory history) {
        return new MyHistoryResult(
                history.reservationId(),
                history.waitingId(),
                ReservationHistoryStatus.valueOf(history.status()),
                history.name(),
                history.date(),
                history.theme(),
                history.time(),
                history.sequence()
        );
    }
}
