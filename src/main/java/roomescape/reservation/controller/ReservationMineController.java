package roomescape.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.LoginMemberRequest;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.util.List;

@RestController
public class ReservationMineController {

    private final ReservationService reservationService;

    public ReservationMineController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("reservations-mine")
    public List<MyReservationResponse> findMyReservations(LoginMemberRequest loginMemberRequest) {
        return reservationService.findReservationsByMember(loginMemberRequest.toLoginMember());
    }
}
