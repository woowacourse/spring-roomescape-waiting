package roomescape.facade;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.dto.request.ServiceReservationTimeCreateRequest;
import roomescape.service.dto.response.ServiceReservationTimeAvailabilityResponse;
import roomescape.service.dto.response.ServiceReservationTimeResponse;

@Service
public class ReservationTimeFacade {

    private final ReservationTimeService reservationTimeService;
    private final ReservationService reservationService;
    private final ThemeService themeService;

    public ReservationTimeFacade(ReservationTimeService reservationTimeService, ReservationService reservationService,
                                 ThemeService themeService) {
        this.reservationTimeService = reservationTimeService;
        this.reservationService = reservationService;
        this.themeService = themeService;
    }

    @Transactional
    public ServiceReservationTimeResponse save(ServiceReservationTimeCreateRequest request) {
        return reservationTimeService.save(request);
    }

    public List<ServiceReservationTimeResponse> findAll() {
        return reservationTimeService.findAll();
    }

    public List<ServiceReservationTimeAvailabilityResponse> findAvailabilityByDateAndTheme(LocalDate date,
                                                                                           Long themeId) {
        themeService.validateExistTheme(themeId);
        return reservationTimeService.findAvailabilityByDateAndTheme(date, themeId);
    }

    @Transactional
    public void delete(Long id) {
        reservationService.validateReferencedTime(id);
        reservationTimeService.delete(id);
    }
}

