package roomescape.reservation.application;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.BookingState;
import roomescape.reservation.ui.dto.response.BookingStateResponse;

@Service
public class ReservationStatusService {

    public List<BookingStateResponse> findAll() {
        return Arrays.stream(BookingState.values())
                .map(BookingStateResponse::from)
                .toList();
    }
}
