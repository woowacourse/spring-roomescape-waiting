package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.RequiredAdmin;
import roomescape.auth.dto.LoginMember;
import roomescape.auth.service.AuthService;
import roomescape.reservation.service.dto.request.AdminReservationRequest;
import roomescape.reservation.service.dto.request.ReservationCreateRequest;
import roomescape.reservation.service.dto.response.ReservationResponse;
import roomescape.reservation.service.CreateReservationService;

import java.net.URI;

@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {

    private final AuthService authService;
    private final CreateReservationService createReservationService;

    public AdminReservationController(
            final AuthService authService,
            final CreateReservationService createReservationService
    ) {
        this.authService = authService;
        this.createReservationService = createReservationService;
    }

    @RequiredAdmin
    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody final AdminReservationRequest request
    ) {
        LoginMember member = authService.findLoginMemberById(request.memberId());
        ReservationCreateRequest reservationCreateRequest =
                new ReservationCreateRequest(request.date(), request.timeId(), request.themeId(), member);
        ReservationResponse response = createReservationService.create(reservationCreateRequest);

        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }
}
