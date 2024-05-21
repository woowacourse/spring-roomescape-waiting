package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.reservation.controller.dto.MyReservationResponse;
import roomescape.reservation.controller.dto.MyReservationWithStatus;

import java.util.List;

@Service
public class WaitingReservationService {

    public List<MyReservationResponse> handleWaitingOrder(List<MyReservationWithStatus> myReservationWithStatuses) {
        return myReservationWithStatuses.stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
