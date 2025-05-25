package roomescape.presentation.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.MyReservationService;
import roomescape.presentation.AuthenticationPrincipal;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.response.MyReservationResponse;
import roomescape.presentation.dto.response.MyReservationWithWaitingResponse;

import java.util.List;

@RestController
public class MyReservationController {

    private final MyReservationService reservationQueryService;

    public MyReservationController(MyReservationService reservationQueryService) {
        this.reservationQueryService = reservationQueryService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationResponse>> getMyReservations(@AuthenticationPrincipal LoginMember loginMember) {
        List<MyReservationResponse> myReservations = reservationQueryService.getMyReservations(loginMember);
        return ResponseEntity.ok(myReservations);
    }

    @GetMapping("/reservations-mine/with-waitings")
    public ResponseEntity<List<MyReservationWithWaitingResponse>> getMyReservationsWithWaiting(@AuthenticationPrincipal LoginMember loginMember) {
        List<MyReservationWithWaitingResponse> myReservations = reservationQueryService.getMyReservationsWithWaitings(loginMember);
        return ResponseEntity.ok(myReservations);
    }
}
