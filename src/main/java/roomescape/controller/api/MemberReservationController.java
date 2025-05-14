package roomescape.controller.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Reservation;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.service.ReservationService;

@RestController
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationResponseDto>> getMyReservations(
            LoginInfo loginInfo
    ) {
        List<MyReservationResponseDto> myReservations = reservationService.findMyReservations(loginInfo);
        return ResponseEntity.ok(myReservations);
    }
}
