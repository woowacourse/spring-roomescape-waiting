package roomescape.application;


import java.util.List;
import roomescape.presentation.dto.request.AvailableReservationTimeRequest;
import roomescape.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.presentation.dto.response.ReservationTimeResponse;
import roomescape.domain.ReservationTime;

public interface ReservationTimeService {

    ReservationTimeResponse createReservationTime(ReservationTimeCreateRequest request);

    List<ReservationTimeResponse> getReservationTimes();

    ReservationTime findReservationTimeById(Long id);

    void deleteReservationTimeById(Long id);

    List<AvailableReservationTimeResponse> getAvailableReservationTimes(AvailableReservationTimeRequest request);
}
