package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Authenticated;
import roomescape.auth.dto.Accessor;
import roomescape.reservation.domain.Status;
import roomescape.reservation.dto.AdminReservationAddRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping(value = "/admin/reservations", params = {"status"})
    public ResponseEntity<List<ReservationResponse>> findAllWaitingReservation(
            @RequestParam(name = "status") Status status) {
        return ResponseEntity.ok(reservationService.findAllWaitingReservation(status));
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(
            @Authenticated Accessor accessor,
            @Valid @RequestBody AdminReservationAddRequest adminReservationAddRequest,
            @RequestParam(name = "status") Status status) {
        ReservationResponse saveResponse = reservationService.saveMemberReservation(
                adminReservationAddRequest.memberId(),
                adminReservationAddRequest.toMemberRequest(),
                status);
        URI createdUri = URI.create("/reservations/" + saveResponse.id());
        return ResponseEntity.created(createdUri).body(saveResponse);
    }
}
