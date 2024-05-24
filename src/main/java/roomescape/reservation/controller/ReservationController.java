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
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.WaitingRequest;
import roomescape.reservation.dto.response.ReservationOrWaitingResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingResponse;
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

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> addWaiting(
            @RequestBody WaitingRequest waitingRequest,
            MemberRequest memberRequest
    ) {
        WaitingResponse waitingResponse = waitingService.addWaiting(waitingRequest, memberRequest);

        return ResponseEntity.created(URI.create("/reservations/waitings/" + waitingResponse.id()))
                .body(waitingResponse);
    }

    @GetMapping
    public List<ReservationResponse> findReservations() {
        return reservationService.findReservations();
    }

    @GetMapping("/waitings")
    public List<WaitingResponse> findWaitings() {
        return waitingService.findAll();
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
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id, MemberRequest memberRequest) {
        reservationService.deleteReservation(id, memberRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable("id") Long id, MemberRequest memberRequest) {
        waitingService.deleteWaiting(id, memberRequest);
        return ResponseEntity.noContent().build();
    }
}
