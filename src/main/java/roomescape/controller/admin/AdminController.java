package roomescape.controller.admin;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservation.WaitingService;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;
import roomescape.service.reservation.dto.WaitingResponse;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public AdminController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> postReservation(
            @RequestBody @Valid ReservationRequest reservationRequest
    ) {
        ReservationResponse reservationResponse = reservationService.createReservation(reservationRequest);
        URI location = UriComponentsBuilder.newInstance()
                .path("/reservations/{id}")
                .buildAndExpand(reservationResponse.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(reservationResponse);
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponse>> getWaitings() {
        return ResponseEntity.ok(waitingService.findAllWaitings());
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent()
                .build();
    }
}
