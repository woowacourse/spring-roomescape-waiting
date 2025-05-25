package roomescape.reservation.presentation;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.login.presentation.dto.SearchCondition;
import roomescape.reservation.application.ReservationApplicationService;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;

@RestController
@RequestMapping("/admin")
public class AdminReservationController {

    private final ReservationApplicationService reservationApplicationService;

    public AdminReservationController(ReservationApplicationService reservationApplicationService) {
        this.reservationApplicationService = reservationApplicationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody AdminReservationRequest request) {
        ReservationResponse response = reservationApplicationService.createReservation(
            new ReservationRequest(
                request.date(),
                request.timeId(),
                request.themeId()
            ), request.memberId());

        return ResponseEntity.created(URI.create("/admin/reservation")).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<ReservationResponse> response = reservationApplicationService.getReservations();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-reservation")
    public ResponseEntity<List<ReservationResponse>> searchReservationWithCondition(@ModelAttribute SearchCondition condition) {
        List<ReservationResponse> responses = reservationApplicationService.searchReservationWithCondition(condition);

        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservationById(@PathVariable("id") final Long id) {
        reservationApplicationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
