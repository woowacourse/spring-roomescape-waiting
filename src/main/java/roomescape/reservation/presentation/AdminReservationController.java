package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Authenticated;
import roomescape.auth.dto.Accessor;
import roomescape.reservation.dto.AdminReservationAddRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(
            @Authenticated Accessor accessor,
            @Valid @RequestBody AdminReservationAddRequest adminReservationAddRequest) {
        ReservationResponse saveResponse = reservationService.saveMemberReservation(
                adminReservationAddRequest.memberId(),
                adminReservationAddRequest.toMemberRequest());
        URI createdUri = URI.create("/reservations/" + saveResponse.id());
        return ResponseEntity.created(createdUri).body(saveResponse);
    }

    @GetMapping("/admin/reservations/waitings")
    public ResponseEntity<List<WaitingResponse>> getWaitings() {
        return ResponseEntity.ok(reservationService.findReservationsOnWaiting());
    }

    @DeleteMapping("/admin/reservations/{id}")
    public ResponseEntity<Void> removeWaiting(@PathVariable("id") Long id) {
        reservationService.removeReservation(id);
        return ResponseEntity.noContent().build();
    }
}
