package roomescape.reservationtime.service;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponseWithBookedStatus;

@Service
@AllArgsConstructor
public class ReservationTimeServiceFacade {

    private final ReservationTimeService reservationTimeService;
    private final ReservationService reservationService;

    @Transactional
    public ReservationTimeResponse createReservationTime(ReservationTimeCreateRequest request) {
        return reservationTimeService.create(request);
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAll() {
        return reservationTimeService.findAll();
    }

    @Transactional
    public void deleteReservationTimeById(Long id) {
        reservationService.validateReservationNonExistenceByTimeId(id);
        reservationTimeService.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponseWithBookedStatus> findAvailableReservationTimesByDateAndThemeId(
        LocalDate date,
        Long themeId
    ) {
        return reservationTimeService.findAvailableReservationTimesByDateAndThemeId(date, themeId);
    }
}
