package roomescape.facade;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.response.ReservationTimeAvailabilityResponse;
import roomescape.controller.dto.response.ReservationTimeResponse;
import roomescape.domain.ReservationTime;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.dto.request.ServiceReservationTimeCreateRequest;

@Service
@Transactional(readOnly = true)
public class ReservationTimeFacade {

    private final ReservationTimeService reservationTimeService;
    private final ReservationService reservationService;
    private final ThemeService themeService;
    private final Clock clock;

    public ReservationTimeFacade(ReservationTimeService reservationTimeService, ReservationService reservationService,
                                 ThemeService themeService, Clock clock) {
        this.reservationTimeService = reservationTimeService;
        this.reservationService = reservationService;
        this.themeService = themeService;
        this.clock = clock;
    }

    @Transactional
    public ReservationTimeResponse save(ServiceReservationTimeCreateRequest request) {
        ReservationTime reservationTime = reservationTimeService.save(request);
        return ReservationTimeResponse.from(reservationTime);
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeService.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<ReservationTimeAvailabilityResponse> findAvailabilityByDateAndTheme(LocalDate date, Long themeId) {
        themeService.validateExistTheme(themeId);
        if (date.isBefore(LocalDate.now(clock))) {
            throw new RoomEscapeException(DomainErrorCode.PAST_RESERVATION_TIME_READ);
        }

        List<ReservationTime> allTimes = reservationTimeService.findAll();
        List<Long> reservedTimeIds = reservationTimeService.findReservedTimeIdsByDateAndTheme(date, themeId);

        return allTimes.stream()
                .map(time -> ReservationTimeAvailabilityResponse.from(time, !reservedTimeIds.contains(time.getId())))
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        reservationService.validateReferencedTime(id);
        reservationTimeService.delete(id);
    }
}
