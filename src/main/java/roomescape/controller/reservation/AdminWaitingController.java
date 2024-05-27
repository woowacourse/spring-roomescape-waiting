package roomescape.controller.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.service.WaitingService;

import java.util.List;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findWaiting() {
        final List<ReservationResponse> waitings = waitingService.findAllWaitings();
        return ResponseEntity.ok(waitings);
    }

    @PutMapping("/{waitingId}")
    public ResponseEntity<ReservationResponse> approveWaiting(@PathVariable final Long waitingId) {
        final ReservationResponse response = waitingService.approve(waitingId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{waitingId}")
    public ResponseEntity<Void> denyWaiting(@PathVariable final Long waitingId) {
        waitingService.deny(waitingId);

        return ResponseEntity.noContent().build();
    }
}
