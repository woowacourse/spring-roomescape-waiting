package roomescape.reservationtime.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeResponse;

@Service
public class ReservationTimeApplicationService {

    private final ReservationTimeDataService reservationTimeDataService;

    public ReservationTimeApplicationService(final ReservationTimeDataService reservationTimeDataService) {
        this.reservationTimeDataService = reservationTimeDataService;
    }

    public ReservationTimeResponse create(final ReservationTimeCreateRequest request) {
        ReservationTime newReservationTime = reservationTimeDataService.create(request.toReservationTime());
        return ReservationTimeResponse.from(newReservationTime);
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeDataService.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAvailable(
            final LocalDate date,
            final Long themeId
    ) {
        return reservationTimeDataService.findAvailable(date, themeId);
    }

    public void removeById(Long id) {
        reservationTimeDataService.delete(id);
    }
}
