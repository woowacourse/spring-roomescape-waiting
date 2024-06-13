package roomescape.controller.reservation;

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
import roomescape.controller.reservation.dto.request.ReservationCreateMemberRequest;
import roomescape.controller.reservation.dto.response.MyReservationWebResponse;
import roomescape.domain.Status;
import roomescape.service.ReservationService;
import roomescape.service.dto.AuthInfo;
import roomescape.service.dto.request.ReservationCreateRequest;
import roomescape.service.dto.response.ReservationResponse;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationWebResponse>> findMyReservations(AuthInfo authInfo) {
        List<MyReservationWebResponse> responses = reservationService.findMyReservations(authInfo)
                .stream()
                .map(MyReservationWebResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody ReservationCreateMemberRequest memberRequest,
             AuthInfo authInfo)
    {
        ReservationCreateRequest reservationCreateRequest = ReservationCreateRequest.of(
                memberRequest,
                authInfo.id(),
                Status.CREATED);
        ReservationResponse reservationResponse = reservationService.save(reservationCreateRequest);
        return ResponseEntity.created(URI.create("/reservation")).body(reservationResponse);
    }

    @PostMapping("/reservations/waiting")
    public ResponseEntity<ReservationResponse> createReservationWaiting(
            @Valid @RequestBody ReservationCreateMemberRequest memberRequest,
            AuthInfo authInfo)
    {
        ReservationCreateRequest reservationCreateRequest = ReservationCreateRequest.of(
                memberRequest,
                authInfo.id(),
                Status.WAITING);
        ReservationResponse reservationResponse = reservationService.save(reservationCreateRequest);
        return ResponseEntity.created(URI.create("/reservation")).body(reservationResponse);
    }

    @DeleteMapping("/reservations/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id, AuthInfo authInfo) {
        reservationService.deleteWaiting(id, authInfo);
        return ResponseEntity.noContent().build();
    }
}
