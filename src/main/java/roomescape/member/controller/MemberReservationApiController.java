package roomescape.member.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.member.dto.MemberResponse;
import roomescape.member.login.authentication.AuthenticationPrincipal;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@Controller
@RequestMapping("/reservation-mine")
public class MemberReservationApiController {
    private final ReservationService reservationService;

    public MemberReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAllByMemberId(
            @AuthenticationPrincipal MemberResponse memberResponse
    ) {
        return ResponseEntity.ok(reservationService.findAllByMemberId(memberResponse.id()));
    }
}
