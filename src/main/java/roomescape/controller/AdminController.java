package roomescape.controller;

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
import roomescape.service.ReservationService;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.WaitingResponse;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;

    public AdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> postReservation(
            @RequestBody @Valid ReservationRequest reservationRequest) {
        ReservationResponse reservationResponse = reservationService.createReservation(reservationRequest,
                reservationRequest.memberId());
        URI location = UriComponentsBuilder.newInstance()
                .path("/reservations/{id}")
                .buildAndExpand(reservationResponse.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(reservationResponse);
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponse>> getWaitings() {
        return ResponseEntity.ok(reservationService.findAllWaitings());
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        reservationService.deleteWaiting(id);
        return ResponseEntity.noContent()
                .build();
    }
}
