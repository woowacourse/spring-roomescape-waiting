package roomescape.admin.reservation.presentation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.reservation.presentation.dto.AdminWaitingReservationResponse;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.presentation.dto.ReservationResponse;

@RestController
@RequestMapping("/reservations/wait")
public class AdminReservationWaitController {

    private final ReservationService reservationService;

    public AdminReservationWaitController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Auth(Role.ADMIN)
    @GetMapping
    public ResponseEntity<List<AdminWaitingReservationResponse>> getWaitingReservation() {
        return ResponseEntity.ok().body(reservationService.getWaitingReservation());
    }

    @Auth(Role.ADMIN)
    @PatchMapping("/accept/{reservationId}")
    public ResponseEntity<ReservationResponse> acceptWaitingReservation(
            final @PathVariable Long reservationId
    ) {
        reservationService.acceptWaitingReservation(reservationId);
        return ResponseEntity.ok().build();
    }
}
