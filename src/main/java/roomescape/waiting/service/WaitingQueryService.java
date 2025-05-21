package roomescape.waiting.service;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.waiting.controller.response.WaitingInfoResponse;
import roomescape.waiting.domain.Waiting;

public interface WaitingQueryService {

    List<WaitingInfoResponse> getAllInfo();

    List<MyReservationResponse> getWaitingReservations(Long memberId);

}
