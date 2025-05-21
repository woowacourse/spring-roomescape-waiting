package roomescape.time.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.time.application.dto.CreateReservationTimeRequest;
import roomescape.time.application.dto.ReservationTimeResponse;
import roomescape.time.application.service.ReservationTimeCommandService;
import roomescape.time.application.service.ReservationTimeQueryService;
import roomescape.time.domain.ReservationTimeId;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationTimeFacadeImpl implements ReservationTimeFacade {

    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ReservationTimeCommandService reservationTimeCommandService;

    @Override
    public List<ReservationTimeResponse> getAll() {
        return ReservationTimeResponse.from(
                reservationTimeQueryService.getAll());
    }

    @Override
    public ReservationTimeResponse create(final CreateReservationTimeRequest request) {
        return ReservationTimeResponse.from(
                reservationTimeCommandService.create(request));
    }

    @Override
    public void delete(final ReservationTimeId id) {
        reservationTimeCommandService.delete(id);
    }
}
