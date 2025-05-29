package roomescape.controller.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationAndWaitingsResponse;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;

@Controller
public class MemberReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public MemberReservationController(ReservationService reservationService, final WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @GetMapping("/reservation-mine")
    public String getMyReservations(
    ) {
        return "reservation-mine";
    }

    @GetMapping("/reservations/me")
    public ResponseEntity<List<MyReservationAndWaitingsResponse>> getMyReservations(
            @CurrentMember LoginInfo loginInfo
    ) {
        List<MyReservationAndWaitingsResponse> reservations = reservationService.findMyReservations(loginInfo.id());
        List<MyReservationAndWaitingsResponse> waitings = waitingService.findMyWaitings(loginInfo.id());
        List<MyReservationAndWaitingsResponse> responses = new ArrayList<>();

        responses.addAll(reservations);
        responses.addAll(waitings);
        responses.sort(Comparator.comparing(MyReservationAndWaitingsResponse::date).thenComparing(MyReservationAndWaitingsResponse::time));

        return ResponseEntity.ok(responses);
    }
}
