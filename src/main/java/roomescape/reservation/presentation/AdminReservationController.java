package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Authenticated;
import roomescape.auth.dto.Accessor;
import roomescape.reservation.dto.AdminReservationAddRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/admin/reservations/waiting")
    public ResponseEntity<List<ReservationResponse>> findAllWaitingReservation() {
        return ResponseEntity.ok(reservationService.findAllWaitingReservation());
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
}
