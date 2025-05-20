package roomescape.member.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/member")
public class MemberController {

    private final ReservationService reservationService;

    public MemberController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<MemberReservationResponse>> getMemberReservations(
            @CookieValue(name = "token", required = false) String token) {
        List<MemberReservationResponse> allMemberReservations = reservationService.findAllMemberReservations(token);
        return ResponseEntity.ok(allMemberReservations);
    }
}
