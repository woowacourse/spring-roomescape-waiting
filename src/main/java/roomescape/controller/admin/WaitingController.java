package roomescape.controller.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.WaitingReservationResponse;
import roomescape.service.reservation.ReservationService;

@RequiredArgsConstructor
@RequestMapping("/admin/waiting/reservation")
@RestController
public class WaitingController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<WaitingReservationResponse>> getWaitingReservations() {
        List<WaitingReservationResponse> waitingReservations = reservationService.getAllWaitingReservations();
        return ResponseEntity.ok(waitingReservations);
    }

    @DeleteMapping("/{reservationId}/deny")
    public ResponseEntity<Void> denyWaitingReservation(@PathVariable Long reservationId) {
        reservationService.denyPendingReservation(reservationId);
        return ResponseEntity.ok().build();
    }
}
