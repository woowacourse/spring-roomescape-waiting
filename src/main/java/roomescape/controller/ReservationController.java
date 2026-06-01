package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMember;
import roomescape.dto.ReservationResult;
import roomescape.dto.WaitingResponseResult;
import roomescape.dto.request.ReservationCreateRequest;
import roomescape.dto.request.ReservationUpdateRequest;
import roomescape.dto.response.MyReservationsAndWaitsResponse;
import roomescape.dto.response.PostReservationWaitResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.WaitingResponse;
import roomescape.service.ReservationService;

import java.net.URI;
import java.util.List;

@RequestMapping("/api/v1/reservations")
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<MyReservationsAndWaitsResponse> getReservations(@LoginMember Long memberId) {
        List<ReservationResult> reservations = reservationService.getReservations(memberId);
        List<WaitingResponseResult> waitingResponseResults = reservationService.getWaitings(memberId);
        MyReservationsAndWaitsResponse myReservationsAndWaitsResponse = new MyReservationsAndWaitsResponse(
                ReservationResponse.fromAll(reservations), WaitingResponse.fromAll(waitingResponseResults));
        return ResponseEntity.ok().body(myReservationsAndWaitsResponse);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest reservationCreateRequest,
            @LoginMember Long memberId) {
        ReservationResult savedReservation = reservationService.createReservation(
                memberId,
                reservationCreateRequest.date(),
                reservationCreateRequest.timeId(),
                reservationCreateRequest.themeId(),
                reservationCreateRequest.storeId()
        );
        ReservationResponse reservationResponse = ReservationResponse.from(savedReservation);
        return ResponseEntity.created(URI.create("/api/v1/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @PostMapping("/{id}/waits")
    public ResponseEntity<PostReservationWaitResponse> createWait(@LoginMember Long memberId, @PathVariable Long id) {
        return ResponseEntity.created(URI.create("/api/v1/reservations/" + id + "/waits"))
                .body(PostReservationWaitResponse.from(reservationService.createWait(memberId, id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest reservationUpdateRequest,
            @LoginMember Long memberId) {
        ReservationResult updatedReservation = reservationService.updateReservation(
                id,
                reservationUpdateRequest.date(),
                memberId,
                reservationUpdateRequest.timeId()
        );
        ReservationResponse reservationResponse = ReservationResponse.from(updatedReservation);
        return ResponseEntity.ok().body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id, @LoginMember Long memberId) {
        reservationService.deleteReservation(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{reservationId}/waits/mine")
    public ResponseEntity<Void> deleteReservationWait(@PathVariable Long reservationId, @LoginMember Long memberId) {
        reservationService.deleteReservationWait(reservationId, memberId);
        return ResponseEntity.noContent().build();
    }
}
