package roomescape.facade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ReservationTimeCreateRequest;
import roomescape.controller.dto.response.ReservationTimeAvailabilityListResponse;
import roomescape.controller.dto.response.ReservationTimeAvailabilityResponse;
import roomescape.controller.dto.response.ReservationTimeListResponse;
import roomescape.controller.dto.response.ReservationTimeResponse;
import roomescape.domain.ReservationAvailability;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waits;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;

@Service
public class ReservationTimeFacade {

    private final ReservationTimeService reservationTimeService;
    private final ReservationService reservationService;
    private final ThemeService themeService;
    private final WaitService waitService;

    public ReservationTimeFacade(ReservationTimeService reservationTimeService, ReservationService reservationService,
                                 ThemeService themeService, WaitService waitService) {
        this.reservationTimeService = reservationTimeService;
        this.reservationService = reservationService;
        this.themeService = themeService;
        this.waitService = waitService;
    }

    @Transactional
    public ReservationTimeResponse save(ReservationTimeCreateRequest request) {
        ReservationTime reservationTimeWithoutId = request.toEntity();
        ReservationTime reservationTime = reservationTimeService.save(reservationTimeWithoutId);
        return ReservationTimeResponse.from(reservationTime);
    }

    public ReservationTimeListResponse findAll() {
        return ReservationTimeListResponse.from(reservationTimeService.findAll());
    }

    public ReservationTimeAvailabilityListResponse findAvailabilityByDateAndTheme(LocalDate date,
                                                                                  Long themeId) {
        themeService.validateExistTheme(themeId);

        List<ReservationTimeAvailabilityResponse> responses = new ArrayList<>();

        List<ReservationTime> reservedTimes = reservationTimeService.findReservedTimesByDateAndTheme(date, themeId);

        for (ReservationTime reservationTime : reservationTimeService.findAll()) {
            responses.add(ReservationTimeAvailabilityResponse.from(reservationTime,
                    getAvailability(reservedTimes, reservationTime, date, themeId)));
        }
        return ReservationTimeAvailabilityListResponse.from(responses);
    }

    @Transactional
    public void delete(Long id) {
        reservationService.validateReferencedTime(id);
        waitService.validateReferencedTime(id);
        reservationTimeService.delete(id);
    }

    private ReservationAvailability getAvailability(List<ReservationTime> reservedTimes,
                                                    ReservationTime reservationTime, LocalDate date, Long themeId) {
        if (!reservedTimes.contains(reservationTime)) {
            return ReservationAvailability.RESERVATION_AVAILABLE;
        }
        Theme theme = themeService.findTheme(themeId);
        Slot slot = new Slot(date, reservationTime, theme);
        Waits waits = waitService.findBySlot(slot);
        if (waits.isFullWaitsBySlot(slot)) {
            return ReservationAvailability.NOTHING_AVAILABLE;
        }
        return ReservationAvailability.WAITING_AVAILABLE;
    }
}

