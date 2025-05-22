package roomescape.reservation.presentation;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.login.presentation.dto.LoginAdminInfo;
import roomescape.auth.login.presentation.dto.SearchCondition;
import roomescape.auth.login.presentation.dto.annotation.LoginAdmin;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<ReservationResponse> response = reservationService.getReservations();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody AdminReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(
            new ReservationRequest(
                request.date(),
                request.timeId(),
                request.themeId()
            ), request.memberId());

        return ResponseEntity.created(URI.create("/admin/reservation")).body(response);
    }

    @GetMapping("/user-reservation")
    public ResponseEntity<List<ReservationResponse>> reservationFilter(@ModelAttribute SearchCondition condition) {
        List<ReservationResponse> responses = reservationService.searchReservationWithCondition(condition);

        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/admin/waitings")
    public ResponseEntity<List<ReservationResponse>> getWaitings(@LoginAdmin LoginAdminInfo adminInfo) {
        return ResponseEntity.ok(reservationService.findAllWaitings());
    }

    @DeleteMapping("/admin/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@LoginAdmin LoginAdminInfo adminInfo, @PathVariable("id") Long waitingId) {
        reservationService.deleteWaiting(waitingId);
        return ResponseEntity.ok().build();
    }
}
