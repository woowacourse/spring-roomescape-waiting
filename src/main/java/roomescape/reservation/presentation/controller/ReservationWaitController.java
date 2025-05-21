package roomescape.reservation.presentation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.admin.AdminWaitingReservationResponse;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;

@RestController
@RequestMapping("/reservations/wait")
public class ReservationWaitController {

    private final ReservationService reservationService;

    public ReservationWaitController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Auth(Role.ADMIN)
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

    @Auth(Role.USER)
    @PatchMapping("/accept/{reservationId}")
    public ResponseEntity<ReservationResponse> acceptWaitingReservation(
            final @PathVariable Long reservationId
    ) {
        reservationService.acceptWaitingReservation(reservationId);
        return ResponseEntity.ok().build();
    }

    private URI createUri(Long reservationId) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservationId)
                .toUri();
    }
}
