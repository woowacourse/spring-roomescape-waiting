package roomescape.controller.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.service.reservation.ReservationQueryService;

@Controller
public class MemberReservationController {

    private final ReservationQueryService reservationQueryService;

    public MemberReservationController(ReservationQueryService reservationQueryService) {
        this.reservationQueryService = reservationQueryService;
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
        List<MyReservationResponseDto> myReservations = reservationQueryService.findMyReservations(loginInfo);
        return ResponseEntity.ok(myReservations);
    }
}
