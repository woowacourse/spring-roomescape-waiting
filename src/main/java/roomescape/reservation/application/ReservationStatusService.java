package roomescape.reservation.application;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.ui.dto.response.ReservationStatusResponse;

@Service
public class ReservationStatusService {

    public List<ReservationStatusResponse> findAll() {
        return Arrays.stream(ReservationStatus.values())
                .map(ReservationStatusResponse::from)
                .toList();
    }
}
