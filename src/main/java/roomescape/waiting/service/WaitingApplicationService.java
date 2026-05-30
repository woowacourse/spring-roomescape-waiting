package roomescape.waiting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.controller.dto.request.WaitingCreateRequest;
import roomescape.waiting.service.dto.response.WaitingCreateResponse;

@Service
@RequiredArgsConstructor
public class WaitingApplicationService {

    private final WaitingService waitingService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public WaitingCreateResponse create(final WaitingCreateRequest request) {
        ReservationTime reservationTime = reservationTimeService.getById(request.timeId());
        Theme theme = themeService.getById(request.themeId());

        final Waiting saved = waitingService.create(
            request.name(),
            request.date(),
            reservationTime,
            theme
        );
        return new WaitingCreateResponse(saved.getId());
    }

    public void deleteByIdAndCustomerName(final long waitingId, final String customerName) {
        waitingService.deleteByIdAndCustomerName(waitingId, customerName);
    }
}
