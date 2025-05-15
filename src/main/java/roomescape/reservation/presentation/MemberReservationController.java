package roomescape.reservation.presentation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.login.presentation.dto.LoginMemberInfo;
import roomescape.auth.login.presentation.dto.annotation.LoginMember;
import roomescape.member.presentation.dto.MyReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationResponse>> getMyReservations(@LoginMember LoginMemberInfo loginMemberInfo) {
        List<MyReservationResponse> response = reservationService.getMemberReservations(loginMemberInfo);

        return ResponseEntity.ok().body(response);
    }
}
