package roomescape.member.controller;

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
import roomescape.member.dto.request.CreateReservationRequest;
import roomescape.member.dto.response.CreateReservationResponse;
import roomescape.member.service.AdminService;
import roomescape.reservation.dto.response.FindReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.waiting.dto.response.FindWaitingResponse;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public AdminController(final AdminService adminService, final ReservationService reservationService,
                           final WaitingService waitingService) {
        this.adminService = adminService;
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<CreateReservationResponse> createReservationByAdmin(
            @Valid @RequestBody CreateReservationRequest createReservationRequest) {
        CreateReservationResponse reservation = adminService.createReservation(createReservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.id())).body(reservation);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<FindReservationResponse>> getReservations() {
        return ResponseEntity.ok(reservationService.getReservations());
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<FindWaitingResponse>> getWaitings() {
        return ResponseEntity.ok(waitingService.getWaitings());
    }

    @DeleteMapping("/waitings/reject/{waitingId}")
    public ResponseEntity<Void> rejectWaiting(@PathVariable Long waitingId) {
        waitingService.rejectWaiting(waitingId);  // TODO: admin??
        return ResponseEntity.noContent().build();
    }
}
