package roomescape.reservationtime.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponseWithBookedStatus;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationTimeServiceFacade {
    private final ReservationTimeService reservationTimeService;
    private final ReservationService reservationService;

    public ReservationTimeServiceFacade(ReservationTimeService reservationTimeService,
                                        ReservationService reservationService) {
        this.reservationTimeService = reservationTimeService;
        this.reservationService = reservationService;
    }

    @Transactional
    public ReservationTimeResponse createReservationTime(ReservationTimeCreateRequest request) {
        return reservationTimeService.createReservationTime(request);
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAll() {
        return reservationTimeService.findAll();
    }

    @Transactional
    public void deleteReservationTimeById(Long id) {
        reservationService.validateReservationNonExistenceByTimeId(id);
        reservationTimeService.deleteReservationTimeById(id);
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponseWithBookedStatus> findAvailableReservationTimesByDateAndThemeId(
            LocalDate date,
            Long themeId
    ) {
        return reservationTimeService.findAvailableReservationTimes(date, themeId);
    }
}
