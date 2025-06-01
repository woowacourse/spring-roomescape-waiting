package roomescape.reservationtime.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateWebRequest;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeWebResponse;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeWebResponse;

@Service
@Transactional
public class ReservationTimeApplicationService {

    private final ReservationTimeDataService reservationTimeDataService;

    public ReservationTimeApplicationService(final ReservationTimeDataService reservationTimeDataService) {
        this.reservationTimeDataService = reservationTimeDataService;
    }

    public ReservationTimeWebResponse create(final ReservationTimeCreateWebRequest request) {
        ReservationTime newReservationTime = reservationTimeDataService.create(request.toReservationTime());
        return ReservationTimeWebResponse.from(newReservationTime);
    }

    public List<ReservationTimeWebResponse> findAll() {
        return reservationTimeDataService.findAll()
                .stream()
                .map(ReservationTimeWebResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeWebResponse> findAvailable(
            final LocalDate date,
            final Long themeId
    ) {
        return reservationTimeDataService.findAvailable(date, themeId);
    }

    public void removeById(Long id) {
        reservationTimeDataService.delete(id);
    }
}
