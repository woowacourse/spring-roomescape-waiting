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
import roomescape.reservation.application.ReservationFacadeService;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.waiting.application.WaitingReservationFacadeService;

@RestController
@RequestMapping("/admin")
public class AdminReservationController {

    private final ReservationFacadeService reservationFacadeService;
    private final WaitingReservationFacadeService waitingReservationFacadeService;

    public AdminReservationController(ReservationFacadeService reservationFacadeService,
                                      WaitingReservationFacadeService waitingReservationFacadeService) {
        this.reservationFacadeService = reservationFacadeService;
        this.waitingReservationFacadeService = waitingReservationFacadeService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<ReservationResponse> response = reservationFacadeService.getReservations();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/waiting-reservations")
    public ResponseEntity<List<ReservationResponse>> getWaitingReservations() {
        List<ReservationResponse> responses = waitingReservationFacadeService.getWaitingReservations();

        return ResponseEntity.ok().body(responses);
    }

    @PostMapping("/waiting-reservations/{id}")
    public ResponseEntity<Void> acceptWaitingReservation(@PathVariable("id") Long waitingId) {
        waitingReservationFacadeService.acceptWaiting(waitingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/waiting-reservations/{id}")
    public ResponseEntity<Void> denyWaitingReservation(@PathVariable("id") Long waitingId) {
        waitingReservationFacadeService.denyWaiting(waitingId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody AdminReservationRequest request) {
        ReservationResponse response = reservationFacadeService.createReservation(
            new ReservationRequest(
                request.date(),
                request.timeId(),
                request.themeId()
            ), request.memberId());

        return ResponseEntity.created(URI.create("/admin/reservation")).body(response);
    }

    @GetMapping("/user-reservation")
    public ResponseEntity<List<ReservationResponse>> reservationFilter(@ModelAttribute SearchCondition condition) {
        List<ReservationResponse> responses = reservationFacadeService.searchReservationWithCondition(condition);

        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservationById(@PathVariable("id") final Long id) {
        reservationFacadeService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
