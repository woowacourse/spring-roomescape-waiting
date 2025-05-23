package roomescape.reservationwaiting.controller;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.service.dto.LoginMemberInfo;
import roomescape.reservationwaiting.controller.dto.MyReservationAndWaitingResponse;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.reservationwaiting.service.ReservationWaitingService;
import roomescape.waiting.service.dto.WaitingInfo;

@RestController
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationAndWaitingResponse>> findAllMyReservationAndWaiting(
            LoginMemberInfo loginMemberInfo
    ) {
        List<ReservationInfo> reservationInfos = reservationWaitingService.findMyReservations(
                loginMemberInfo.id());
        List<WaitingInfo> waitingInfos = reservationWaitingService.findMyWaiting(loginMemberInfo.id());
        List<MyReservationAndWaitingResponse> responses = Stream.concat(
                reservationInfos.stream().map(MyReservationAndWaitingResponse::new),
                waitingInfos.stream().map(MyReservationAndWaitingResponse::new)
        ).toList();
        return ResponseEntity.ok().body(responses);
    }
}
