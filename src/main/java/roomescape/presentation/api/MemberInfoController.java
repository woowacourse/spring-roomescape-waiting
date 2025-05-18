package roomescape.presentation.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.presentation.AuthenticationPrincipal;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.response.MyReservationResponse;

import java.util.List;

@RestController
public class MemberInfoController {

    private final ReservationService reservationService;

    public MemberInfoController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationResponse>> getMyReservations(@AuthenticationPrincipal LoginMember loginMember) {
        List<MyReservationResponse> myReservations = reservationService.getMyReservations(loginMember);
        return ResponseEntity.ok(myReservations);
    }
}
