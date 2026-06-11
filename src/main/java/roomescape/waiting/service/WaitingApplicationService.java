package roomescape.waiting.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.waiting.controller.dto.request.WaitingCreateRequest;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.exception.NoReservationForWaitingException;
import roomescape.waiting.repository.dto.WaitingWithRank;
import roomescape.waiting.service.dto.response.WaitingCreateResponse;

@Service
@RequiredArgsConstructor
public class WaitingApplicationService {

    private final WaitingService waitingService;
    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    @Transactional
    public WaitingCreateResponse create(final WaitingCreateRequest request) {
        ReservationTime reservationTime = reservationTimeService.getById(request.timeId());
        Theme theme = themeService.getById(request.themeId());

        final Optional<Reservation> reservation = reservationService.findBySlotForUpdate(
            request.date(),
            reservationTime.getId(),
            theme.getId()
        );
        if (reservation.isEmpty()) {
            throw new NoReservationForWaitingException();
        }

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

    public List<WaitingWithRank> findAllWithRank() {
        return waitingService.findAllWithRank();
    }
}
