package roomescape.waiting.service;

import java.util.List;
import roomescape.reservation.controller.response.MyReservationResponse;

public interface WaitingQueryService {
    List<MyReservationResponse> getWaitingReservations(Long memberId);
}
