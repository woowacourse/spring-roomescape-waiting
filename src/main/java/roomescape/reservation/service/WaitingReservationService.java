package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.reservation.controller.dto.MyReservationResponse;
import roomescape.reservation.controller.dto.MyReservationWithStatus;

import java.util.List;

@Service
public class WaitingReservationService {


    public List<MyReservationResponse> handleWaitingOrder(List<MyReservationWithStatus> myReservationWithStatuses) {
        // TODO 예약 상태가 waiting 인 애들의 예약 대기 순서를 알아내서 처리해줌
        return myReservationWithStatuses.stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
