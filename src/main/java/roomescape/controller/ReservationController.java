package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMemberId;
import roomescape.service.auth.AuthService;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationCreateService reservationCreateService;
    private final AuthService authService;

    public ReservationController(ReservationService reservationService, ReservationCreateService reservationCreateService, AuthService authService) {
        this.reservationService = reservationService;
        this.reservationCreateService = reservationCreateService;
        this.authService = authService;
    }

    @GetMapping
    public List<ReservationResponse> findAll() {
        return reservationService.findAll();
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody @Valid ReservationRequest reservationRequest,
            @LoginMemberId long memberId) {
        ReservationResponse reservationResponse = reservationCreateService.createMemberReservation(reservationRequest, memberId);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") long reservationId, @LoginMemberId long memberId) {
        authService.validateAdmin(memberId);
        reservationService.deleteById(reservationId);
        return ResponseEntity.noContent().build();
    }
}
