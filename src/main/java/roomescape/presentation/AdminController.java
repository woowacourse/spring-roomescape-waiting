package roomescape.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.domain.reservation.Status;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.ReservationCriteriaRequest;
import roomescape.dto.ReservationResponse;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final ReservationService reservationService;

    public AdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<ReservationResponse>> findAllByWaiting() {
        List<ReservationResponse> responses = reservationService.findAllByStatus(Status.WAITING);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> saveReservationByAdmin(@RequestBody @Valid AdminReservationRequest adminReservationRequest) {
        ReservationResponse reservationResponse = reservationService.saveByAdmin(adminReservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<List<ReservationResponse>> searchAdmin(ReservationCriteriaRequest reservationCriteriaRequest) {
        List<ReservationResponse> responses = reservationService.findByCriteria(reservationCriteriaRequest);
        return ResponseEntity.ok(responses);
    }
}
