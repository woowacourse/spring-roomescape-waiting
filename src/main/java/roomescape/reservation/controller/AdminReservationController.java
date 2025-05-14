package roomescape.reservation.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import roomescape.auth.annotation.RequiredAdmin;
import roomescape.auth.service.AuthService;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {

    private final AuthService authService;
    private final ReservationService reservationService;

    public AdminReservationController(final AuthService authService, final ReservationService reservationService) {
        this.authService = authService;
        this.reservationService = reservationService;
    }

    @RequiredAdmin
    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody final AdminReservationRequest request
    ) {
        Member member = authService.findById(request.memberId());
        ReservationRequest reservationRequest =
                new ReservationRequest(request.date(), request.timeId(), request.themeId());
        ReservationResponse response = reservationService.create(reservationRequest, member);

        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }
}
