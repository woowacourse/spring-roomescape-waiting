package roomescape.web.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationWaitingService;
import roomescape.service.response.ReservationWaitingAppResponse;
import roomescape.web.controller.request.ReservationWaitingStatusWebRequest;
import roomescape.web.controller.response.ReservationWaitingWebResponse;

import java.util.List;

@RestController
@RequestMapping("/admin/reservation-waitings")
public class AdminReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public AdminReservationWaitingController(final ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationWaitingWebResponse>> getAvailableWaitings() {
        List<ReservationWaitingWebResponse> waitingWebResponses = reservationWaitingService.findAllAllowed()
                .stream()
                .map(ReservationWaitingWebResponse::new)
                .toList();

        return ResponseEntity.ok().body(waitingWebResponses);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationWaitingWebResponse> updateWaitingStatus(@PathVariable Long id, @Valid @RequestBody ReservationWaitingStatusWebRequest request) {
        ReservationWaitingAppResponse waitingAppResponse = reservationWaitingService.updateWaitingStatus(id, request.status());
        ReservationWaitingWebResponse waitingWebResponse = new ReservationWaitingWebResponse(waitingAppResponse);

        return ResponseEntity.ok(waitingWebResponse);
    }
}
