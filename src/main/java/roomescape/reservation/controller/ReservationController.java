package roomescape.reservation.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.MemberRequest;
import roomescape.reservation.dto.ReservationOrWaitingResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody ReservationRequest reservationRequest,
            MemberRequest memberRequest
    ) {
        ReservationResponse reservationResponse = reservationService.addReservation(reservationRequest, memberRequest);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @PostMapping("/waiting")
    public ResponseEntity<WaitingResponse> addWaiting(
            @RequestBody ReservationRequest reservationRequest,
            MemberRequest memberRequest
    ) {
        WaitingResponse waitingResponse = waitingService.addWaiting(reservationRequest, memberRequest);

        return ResponseEntity.created(URI.create("/reservations/waiting/" + waitingResponse.id()))
                .body(waitingResponse);
    }

    @GetMapping
    public List<ReservationResponse> findReservations() {
        return reservationService.findReservations();
    }

    @GetMapping("/mine")
    public List<ReservationOrWaitingResponse> findReservationsByMember(MemberRequest memberRequest) {
        List<ReservationOrWaitingResponse> reservations = reservationService.findReservationsByMember(memberRequest);
        List<ReservationOrWaitingResponse> waitings = waitingService.findWaitingsByMember(memberRequest);

        return combineReservationsAndWaitings(reservations, waitings);
    }

    private List<ReservationOrWaitingResponse> combineReservationsAndWaitings(
            List<ReservationOrWaitingResponse> reservations,
            List<ReservationOrWaitingResponse> waitings
    ) {
        List<ReservationOrWaitingResponse> result = new ArrayList<>(reservations);
        result.addAll(waitings);

        return result;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
