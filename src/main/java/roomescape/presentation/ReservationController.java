package roomescape.presentation;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.application.dto.LoginMember;
import roomescape.application.dto.MyReservationResponse;
import roomescape.application.dto.ReservationCriteria;
import roomescape.application.dto.ReservationRequest;
import roomescape.application.dto.ReservationResponse;
import roomescape.infrastructure.authentication.AuthenticationPrincipal;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAll() {
        List<ReservationResponse> responses = reservationService.findAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<List<ReservationResponse>> findAllForAdmin(ReservationCriteria reservationCriteria) {
        List<ReservationResponse> responses = reservationService.findByCriteria(reservationCriteria);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.create(loginMember, request);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations/my")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(
            @AuthenticationPrincipal LoginMember loginMember) {
        List<MyReservationResponse> responses = reservationService.findMyReservationsAndWaiting(loginMember.id());
        return ResponseEntity.ok(responses);
    }
}
