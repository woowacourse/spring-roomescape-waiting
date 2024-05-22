package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.request.ReservationFilterRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/admin")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createAdminReservation(
            @Valid @RequestBody AdminReservationRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ReservationResponse reservationResponse = reservationService.create(request, now);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAllByMemberAndThemeAndPeriod(
            ReservationFilterRequest request) {
        return ResponseEntity.ok(reservationService.findDistinctReservations(request));
    }

    @GetMapping("/reservations/waiting")
    public ResponseEntity<List<ReservationResponse>> findPendingReservations() {
        return ResponseEntity.ok(reservationService.findAllPending());
    }
}
