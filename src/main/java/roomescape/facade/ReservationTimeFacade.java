package roomescape.facade;

import static roomescape.service.WaitService.MAX_WAITING_COUNT;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationAvailability;
import roomescape.domain.ReservationTime;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;
import roomescape.service.dto.request.ServiceReservationTimeCreateRequest;
import roomescape.service.dto.response.ServiceReservationTimeAvailabilityResponse;
import roomescape.service.dto.response.ServiceReservationTimeResponse;

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
    public ServiceReservationTimeResponse save(ServiceReservationTimeCreateRequest request) {
        ReservationTime reservationTimeWithoutId = request.toEntity();
        ReservationTime reservationTime = reservationTimeService.save(reservationTimeWithoutId);
        return ServiceReservationTimeResponse.from(reservationTime);
    }

    public List<ServiceReservationTimeResponse> findAll() {
        return reservationTimeService.findAll().stream()
                .map(ServiceReservationTimeResponse::from)
                .toList();
    }

    public List<ServiceReservationTimeAvailabilityResponse> findAvailabilityByDateAndTheme(LocalDate date,
                                                                                           Long themeId) {
        themeService.validateExistTheme(themeId);

        List<ServiceReservationTimeAvailabilityResponse> responses = new ArrayList<>();

        List<ReservationTime> reservedTimes = reservationTimeService.findReservedTimesByDateAndTheme(date, themeId);

        for (ReservationTime reservationTime : reservationTimeService.findAll()) {
            responses.add(ServiceReservationTimeAvailabilityResponse.from(reservationTime,
                    getAvailability(reservedTimes, reservationTime, date, themeId)));
        }
        return responses;
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
        if (waitService.findBySlot(date, reservationTime.getId(), themeId).size() >= MAX_WAITING_COUNT) {
            return ReservationAvailability.NOTHING_AVAILABLE;
        }
        return ReservationAvailability.WAITING_AVAILABLE;
    }
}

