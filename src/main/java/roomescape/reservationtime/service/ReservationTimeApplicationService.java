package roomescape.reservationtime.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.service.ReservationDomainService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;

@Service
public class ReservationTimeApplicationService {

    private final ReservationTimeDomainService reservationTimeDomainService;
    private final ReservationDomainService reservationDomainService;

    public ReservationTimeApplicationService(final ReservationTimeDomainService reservationTimeDomainService,
                                             final ReservationDomainService reservationDomainService) {
        this.reservationTimeDomainService = reservationTimeDomainService;
        this.reservationDomainService = reservationDomainService;
    }

    public List<ReservationTimeResponse> getReservationTimes() {
        return reservationTimeDomainService.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public void delete(Long id) {
        reservationTimeDomainService.delete(id);
    }

    public ReservationTimeResponse create(final ReservationTimeCreateRequest request) {
        ReservationTime newReservationTime = reservationTimeDomainService.save(request.toReservationTime());
        return ReservationTimeResponse.from(newReservationTime);
    }

    public List<AvailableReservationTimeResponse> getAvailableReservationTimes(final LocalDate date,
                                                                               final Long themeId) {
        return reservationDomainService.findBookedTimesByDateAndThemeId(date, themeId);
    }
}
