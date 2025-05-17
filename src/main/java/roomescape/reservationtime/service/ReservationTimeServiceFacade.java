package roomescape.reservationtime.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponseWithBookedStatus;

@Service
public class ReservationTimeServiceFacade {
    private final ReservationTimeService reservationTimeService;
    private final ReservationService reservationService;

    public ReservationTimeServiceFacade(ReservationTimeService reservationTimeService,
                                        ReservationService reservationService) {
        this.reservationTimeService = reservationTimeService;
        this.reservationService = reservationService;
    }

    public ReservationTimeResponse createReservationTime(ReservationTimeCreateRequest request) {
        return reservationTimeService.createReservationTime(request);
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeService.findAll();
    }

    public void deleteReservationTimeById(Long id) {
        reservationService.validateReservationNonExistenceByTimeId(id);
        reservationTimeService.deleteReservationTimeById(id);
    }

    public List<ReservationTimeResponseWithBookedStatus> findAvailableReservationTimesByDateAndThemeId(
        LocalDate date,
        Long themeId
    ) {
        return reservationTimeService.findAvailableReservationTimesByDateAndThemeId(date, themeId);
    }
}
