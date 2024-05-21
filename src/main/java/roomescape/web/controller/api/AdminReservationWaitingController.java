package roomescape.web.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationWaitingService;
import roomescape.web.controller.response.ReservationWaitingWebResponse;

import java.util.List;

@RestController
@RequestMapping("/admin/waitings")
public class AdminReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public AdminReservationWaitingController(final ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationWaitingWebResponse>> getWaitings() {
        List<ReservationWaitingWebResponse> waitingWebResponses = reservationWaitingService.findAll()
                .stream()
                .map(ReservationWaitingWebResponse::new)
                .toList();

        return ResponseEntity.ok().body(waitingWebResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        reservationWaitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
