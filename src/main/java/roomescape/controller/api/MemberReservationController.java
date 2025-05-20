package roomescape.controller.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.service.ReservationService;

@Controller
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservation-mine")
    public String getMyReservations(
    ) {
        return "reservation-mine";
    }

    @GetMapping("/reservations/me")
    public ResponseEntity<List<MyReservationResponseDto>> getMyReservations(
            @CurrentMember LoginInfo loginInfo
    ) {
        List<MyReservationResponseDto> myReservations = reservationService.findMyReservations(loginInfo);
        return ResponseEntity.ok(myReservations);
    }
}
