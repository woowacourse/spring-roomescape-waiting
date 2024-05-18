package roomescape.presentation.api;

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
import roomescape.application.ReservationService;
import roomescape.application.dto.request.ReservationRequest;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.presentation.dto.request.AdminReservationWebRequest;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;

    public AdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addAdminReservation(
            @RequestBody @Valid AdminReservationWebRequest webRequest
    ) {
        ReservationRequest reservationRequest = webRequest.toReservationRequest();
        ReservationResponse reservationResponse = reservationService.addReservation(reservationRequest);

        return ResponseEntity.created(URI.create("/reservation/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservationById(@PathVariable Long id) {
        reservationService.deleteReservationById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations/waiting")
    public ResponseEntity<List<ReservationResponse>> getReservationWaitings() {
        List<ReservationResponse> reservationResponses = reservationService.getReservationWaitings();

        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping("/reservations/waiting/{id}/approve")
    public ResponseEntity<ReservationResponse> approveReservationWaiting(@PathVariable Long id) {
        ReservationResponse reservationResponse = reservationService.approveReservationWaiting(id);

        return ResponseEntity.ok(reservationResponse);
    }

    @DeleteMapping("/reservations/waiting/{id}/reject")
    public ResponseEntity<Void> rejectReservationWaiting(@PathVariable Long id) {
        reservationService.rejectReservationWaiting(id);

        return ResponseEntity.noContent().build();
    }
}
