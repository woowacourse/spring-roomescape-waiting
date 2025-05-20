package roomescape.reservation.presentation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.presentation.dto.AdminWaitingReservationResponse;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;

@RestController
@RequestMapping("/reservations/wait")
public class ReservationWaitController {

    private final ReservationService reservationService;

    public ReservationWaitController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Auth(Role.USER)
    @GetMapping
    public ResponseEntity<List<AdminWaitingReservationResponse>> getWaitingReservation() {
        return ResponseEntity.ok().body(reservationService.getWaitingReservation());
    }

    @Auth(Role.USER)
    @PostMapping
    public ResponseEntity<ReservationResponse> createWaitingReservation(
            final @RequestBody @Valid ReservationRequest request,
            final Long memberId
    ) {
        ReservationResponse reservation = reservationService.createWaitingReservation(request, memberId);

        return ResponseEntity.created(createUri(reservation.getId()))
                .body(reservation);
    }

    private URI createUri(Long reservationId) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservationId)
                .toUri();
    }
}
