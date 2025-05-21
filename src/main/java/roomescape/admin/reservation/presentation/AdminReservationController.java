package roomescape.admin.reservation.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.admin.reservation.presentation.dto.AdminReservationRequest;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.presentation.dto.ReservationResponse;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Auth(Role.ADMIN)
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            final @RequestBody @Valid AdminReservationRequest adminReservationRequest
    ) {
        ReservationResponse reservation = reservationService.createReservation(adminReservationRequest);

        return ResponseEntity.created(createUri(reservation.getId()))
                .body(reservation);
    }

    @Auth(Role.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            final @PathVariable Long id
    ) {
        reservationService.deleteReservationByAdmin(id);
        return ResponseEntity.noContent().build();
    }

    private URI createUri(Long reservationId) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservationId)
                .toUri();
    }
}
