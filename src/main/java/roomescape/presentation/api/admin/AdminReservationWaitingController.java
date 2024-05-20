package roomescape.presentation.api.admin;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.application.dto.response.ReservationResponse;

@RestController
@RequestMapping("/admin/reservations/waiting")
public class AdminReservationWaitingController {

    private final ReservationService reservationService;

    public AdminReservationWaitingController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservationWaitings() {
        List<ReservationResponse> reservationResponses = reservationService.getReservationWaitings();

        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ReservationResponse> approveReservationWaiting(@PathVariable Long id) {
        ReservationResponse reservationResponse = reservationService.approveReservationWaiting(id);

        return ResponseEntity.ok(reservationResponse);
    }

    @DeleteMapping("/{id}/reject")
    public ResponseEntity<Void> rejectReservationWaiting(@PathVariable Long id) {
        reservationService.rejectReservationWaiting(id);

        return ResponseEntity.ok().build();
    }
}
