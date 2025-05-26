package roomescape.reservationtime.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeResponse;

@Service
public class ReservationTimeApplicationService {

    private final ReservationTimeDataService reservationTimeDataService;
    private final ReservationSlotDataService reservationSlotDataService;

    public ReservationTimeApplicationService(final ReservationTimeDataService reservationTimeDataService,
                                             final ReservationSlotDataService reservationSlotDataService) {
        this.reservationTimeDataService = reservationTimeDataService;
        this.reservationSlotDataService = reservationSlotDataService;
    }

    public List<ReservationTimeResponse> getReservationTimes() {
        return reservationTimeDataService.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public void delete(Long id) {
        reservationTimeDataService.delete(id);
    }

    public ReservationTimeResponse create(final ReservationTimeCreateRequest request) {
        ReservationTime newReservationTime = reservationTimeDataService.save(request.toReservationTime());
        return ReservationTimeResponse.from(newReservationTime);
    }

    public List<AvailableReservationTimeResponse> getAvailableReservationTimes(final LocalDate date,
                                                                               final Long themeId) {
        return reservationSlotDataService.findBookedTimesByDateAndThemeId(date, themeId);
    }
}
