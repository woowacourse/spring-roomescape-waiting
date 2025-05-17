package roomescape.time.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.time.application.dto.CreateReservationTimeServiceRequest;
import roomescape.time.application.service.ReservationTimeCommandService;
import roomescape.time.application.service.ReservationTimeQueryService;
import roomescape.time.domain.ReservationTimeId;
import roomescape.time.ui.dto.CreateReservationTimeWebRequest;
import roomescape.time.ui.dto.ReservationTimeResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationTimeFacadeImpl implements ReservationTimeFacade {

    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ReservationTimeCommandService reservationTimeCommandService;

    @Override
    public List<ReservationTimeResponse> getAll() {
        return ReservationTimeResponse.from(
                reservationTimeQueryService.getAll());
    }

    @Override
    public ReservationTimeResponse create(final CreateReservationTimeWebRequest request) {
        return ReservationTimeResponse.from(
                reservationTimeCommandService.create(
                        new CreateReservationTimeServiceRequest(
                                request.startAt())));
    }

    @Override
    public void delete(final Long id) {
        reservationTimeCommandService.delete(ReservationTimeId.from(id));
    }
}
