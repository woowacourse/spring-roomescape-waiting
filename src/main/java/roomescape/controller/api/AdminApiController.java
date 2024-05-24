package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.api.dto.request.AdminReservationRequest;
import roomescape.controller.api.dto.response.ReservationResponse;
import roomescape.controller.api.dto.response.WaitingsResponse;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;
import roomescape.service.dto.output.ReservationOutput;
import roomescape.service.dto.output.WaitingOutput;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminApiController {
    private final WaitingService waitingService;
    private final ReservationService reservationService;

    public AdminApiController(final WaitingService waitingService, final ReservationService reservationService) {
        this.waitingService = waitingService;
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody final AdminReservationRequest request) {
        final ReservationOutput output = reservationService.createReservation(request.toInput());
        return ResponseEntity.created(URI.create("/reservations/" + output.id()))
                .body(ReservationResponse.from(output));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable final long id) {
        reservationService.deleteReservationWithAdmin(id);
        return ResponseEntity.noContent()
                .build();
    }

    @GetMapping("/waitings")
    public ResponseEntity<WaitingsResponse> getAllWaiting() {
        final List<WaitingOutput> outputs = waitingService.getAllWaiting();
        return ResponseEntity.ok(WaitingsResponse.from(outputs));
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> cancelWaiting(@PathVariable final long id) {
        waitingService.deleteWaitingWithAdmin(id);
        return ResponseEntity.noContent()
                .build();
    }
}
