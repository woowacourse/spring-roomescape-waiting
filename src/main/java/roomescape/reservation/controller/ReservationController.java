package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import roomescape.auth.principal.AuthenticatedMember;
import roomescape.reservation.dto.*;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingService;
import roomescape.resolver.Authenticated;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationController(final ReservationService reservationService, final WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> getReservations() {
        return reservationService.getReservations()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(
            @Valid @RequestBody final SaveReservationRequest request,
            @Authenticated final AuthenticatedMember authenticatedMember
    ) {
        final Reservation savedReservation = reservationService.saveReservation(
                request.setMemberId(authenticatedMember.id()));

        return ResponseEntity.created(URI.create("/reservations/" + savedReservation.getId()))
                .body(ReservationResponse.from(savedReservation));
    }

    @GetMapping("/reservations-mine")
    public List<MyReservationResponse> getMyReservations(@Authenticated final AuthenticatedMember authenticatedMember) {
        return reservationService.getMyReservations(authenticatedMember.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();
        return reservationService.getMyReservations(authenticatedMember.id());
//        return reservationService.getMyReservations(authenticatedMember.id())
//                .stream()
//                .map(MyReservationResponse::from)
//                .toList();
    }

    @PostMapping("/reservations-waiting")
    public ResponseEntity<WaitingResponse> saveWaiting(
            @RequestBody final WaitingRequest request,
            @Authenticated final AuthenticatedMember authenticatedMember
    ) {
        Waiting savedWaiting = waitingService.saveWaiting(request, authenticatedMember.id());

        return ResponseEntity.created(URI.create("/reservations-waiting/" + savedWaiting.getId()))
                .body(WaitingResponse.from(savedWaiting));
    }

    }
}
