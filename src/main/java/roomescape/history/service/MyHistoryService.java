package roomescape.history.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.history.MyHistory;
import roomescape.history.ReservationHistoryStatus;
import roomescape.history.controller.dto.HistoryResponse;
import roomescape.history.repository.MyHistoryRepository;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

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
