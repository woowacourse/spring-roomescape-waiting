package roomescape.application.reservationwaiting.controller;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservationwaiting.controller.dto.MyReservationAndWaitingResponse;
import roomescape.application.reservationwaiting.service.ReservationWaitingService;
import roomescape.application.reservationwaiting.service.dto.ReservationInfoAndWaitingInfo;
import roomescape.member.service.dto.LoginMemberInfo;

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
        ReservationInfoAndWaitingInfo myReservationAndWaiting = reservationWaitingService.findMyReservationAndWaiting(
                loginMemberInfo.id());
        List<MyReservationAndWaitingResponse> responses = Stream.concat(
                myReservationAndWaiting.reservationInfos()
                        .stream()
                        .map(MyReservationAndWaitingResponse::new),
                myReservationAndWaiting.waitingInfos()
                        .stream()
                        .map(MyReservationAndWaitingResponse::new)
        ).toList();
        return ResponseEntity.ok().body(responses);
    }
}
