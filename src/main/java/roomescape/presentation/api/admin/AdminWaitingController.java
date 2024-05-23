package roomescape.presentation.api.admin;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.WaitingService;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.application.dto.response.WaitingResponse;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> getReservationWaitings() {
        List<WaitingResponse> waitingResponses = waitingService.getWaitings();

        return ResponseEntity.ok(waitingResponses);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ReservationResponse> approveReservationWaiting(@PathVariable Long id) {
        ReservationResponse reservationResponse = waitingService.approveWaitingToReservation(id);

        return ResponseEntity.ok(reservationResponse);
    }

    @DeleteMapping("/{id}/reject")
    public ResponseEntity<Void> rejectReservationWaiting(@PathVariable Long id) {
        waitingService.rejectWaitingToReservation(id);

        return ResponseEntity.ok().build();
    }
}
