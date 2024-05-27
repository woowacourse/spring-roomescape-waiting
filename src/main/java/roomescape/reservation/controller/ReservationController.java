package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.principal.AuthenticatedMember;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.SaveReservationRequest;
import roomescape.reservation.dto.ReservationDto;
import roomescape.reservation.service.ReservationService;
import roomescape.resolver.Authenticated;

import java.net.URI;
import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations-mine")
    public List<MyReservationResponse> getMyReservations(@Authenticated final AuthenticatedMember authenticatedMember) {
        return reservationService.getMyReservations(authenticatedMember.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(
            @RequestBody final SaveReservationRequest request,
            @Authenticated final AuthenticatedMember authenticatedMember
    ) {
        final ReservationDto savedReservation = reservationService.saveReservation(
                request.setMemberId(authenticatedMember.id()));
        final ReservationResponse response = ReservationResponse.from(savedReservation);

        return ResponseEntity.created(URI.create("/reservations/" + savedReservation.id()))
                .body(response);
    }
}
