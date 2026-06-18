package roomescape.service.reservationmine;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.service.history.MyHistoryService;

@Service
public class ReservationMineService {

    private static final String RESERVATION_STATUS = "RESERVATION";

    private final MyHistoryService myHistoryService;

    public ReservationMineService(final MyHistoryService myHistoryService) {
        this.myHistoryService = myHistoryService;
    }

    public List<ReservationResponse> getAllByName(final String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }

        return myHistoryService.getAllByName(name).stream()
                .filter(history -> RESERVATION_STATUS.equals(history.status()))
                .map(history -> new ReservationResponse(
                        history.reservationId(),
                        history.name(),
                        history.date(),
                        ThemeResponse.from(history.theme()),
                        ReservationTimeResponse.from(history.time())
                ))
                .toList();
    }
}
