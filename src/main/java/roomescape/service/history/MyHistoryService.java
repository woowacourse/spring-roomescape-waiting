package roomescape.service.history;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.controller.history.ReservationHistoryStatus;
import roomescape.controller.history.dto.HistoryResponse;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.repository.history.MyHistory;
import roomescape.repository.history.MyHistoryRepository;

@Service
public class MyHistoryService {

    private final MyHistoryRepository myHistoryRepository;

    public MyHistoryService(final MyHistoryRepository myHistoryRepository) {
        this.myHistoryRepository = myHistoryRepository;
    }

    public List<HistoryResponse> getAllByName(final String name) {
        return myHistoryRepository.findByUserName(name).stream()
                .map(this::toResponse)
                .toList();
    }

    private HistoryResponse toResponse(final MyHistory history) {
        return new HistoryResponse(
                history.reservationId(),
                history.waitingId(),
                ReservationHistoryStatus.valueOf(history.status()),
                history.name(),
                history.date(),
                ThemeResponse.from(history.theme()),
                ReservationTimeResponse.from(history.time()),
                history.sequence()
        );
    }
}
