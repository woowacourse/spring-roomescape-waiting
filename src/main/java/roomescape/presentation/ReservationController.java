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
import roomescape.config.LoginMemberConverter;
import roomescape.domain.reservation.Status;
import roomescape.dto.LoginMember;
import roomescape.dto.MyReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;

@RestController
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAllByReservation() {
        List<ReservationResponse> responses = reservationService.findAllByStatus(Status.RESERVATION);
        return ResponseEntity.ok(responses);
    }

    @PostMapping(value = {"/reservations", "/waitings"})
    public ResponseEntity<ReservationResponse> saveReservationByClient(
            @LoginMemberConverter LoginMember loginMember,
            @RequestBody ReservationRequest reservationRequest) {
        ReservationResponse response = reservationService.saveByClient(loginMember, reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @DeleteMapping(value = {"/reservations/{id}", "/waitings/{id}"})
    public ResponseEntity<Void> deleteByReservation(@PathVariable long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations/mine")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(
            @LoginMemberConverter LoginMember loginMember) {
        List<MyReservationResponse> responses = reservationService.findMyReservations(loginMember.id());
        return ResponseEntity.ok(responses);
    }
}
