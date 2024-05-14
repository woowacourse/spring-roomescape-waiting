package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ReservationResponse createReservation(
            @Valid @RequestBody MemberReservationCreateRequest request,
            LoginMember member
    ) {
        return reservationService.createReservation(request, member);
    }
}
