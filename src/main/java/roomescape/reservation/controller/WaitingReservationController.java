package roomescape.reservation.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationDetailService;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.ReservationWaitingService;

@RestController
@RequestMapping("/waiting-reservations")
public class WaitingReservationController {
    private final ReservationDetailService reservationDetailService;
    private final ReservationWaitingService waitingService;
    private final ReservationService reservationService;

    public WaitingReservationController(ReservationDetailService reservationDetailService, ReservationWaitingService waitingService, ReservationService reservationService) {
        this.reservationDetailService = reservationDetailService;
        this.waitingService = waitingService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservations() {
        List<ReservationResponse> response = waitingService.findReservationWaitings();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createWaitingReservation(ReservationCreateRequest request) {
        Long detailId = reservationDetailService.findReservationDetailId(request);
        ReservationRequest reservationRequest = new ReservationRequest(request.memberId(), detailId);

        reservationService.findReservationByDetailId(reservationRequest);
        ReservationResponse reservationCreateResponse = waitingService.addReservationWaiting(reservationRequest);

        URI uri = URI.create("/waiting-reservations/" + reservationCreateResponse.id());
        return ResponseEntity.created(uri)
                .body(reservationCreateResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationWaiting(@PathVariable long id) {
        waitingService.removeReservations(id);
        return ResponseEntity.noContent()
                .build();
    }
}
