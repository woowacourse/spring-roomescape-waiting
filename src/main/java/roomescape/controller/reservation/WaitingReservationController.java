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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.reservation.dto.CreateReservationRequest;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations/waiting")
public class WaitingReservationController {

    private final ReservationService reservationService;

    public WaitingReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> getWaitingReservations() {
        return reservationService.getWaitingReservations();
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addWaitingReservation(
            @RequestBody @Valid final CreateReservationRequest reservationRequest,
            @Valid final LoginMember loginMember) {
        final ReservationResponse reservation
                = reservationService.addWaiting(reservationRequest, loginMember.id());

        final URI uri = UriComponentsBuilder.fromPath("/reservations/waiting/{id}")
                .buildAndExpand(reservation.id())
                .toUri();
        return ResponseEntity.created(uri).body(reservation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@PathVariable("id") final Long id) {
        reservationService.deleteWaiting(id);
        return ResponseEntity.noContent()
                .build();
    }
}
