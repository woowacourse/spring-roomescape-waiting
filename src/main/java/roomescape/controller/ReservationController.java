package roomescape.controller;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.domain.Reservation;
import roomescape.dto.request.ReservationCreateRequest;
import roomescape.dto.request.ReservationUpdateRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationWaitResponse;
import roomescape.service.ReservationService;

@RequestMapping("/api/v1/reservations")
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations(@LoginMember Long memberId) {
        List<Reservation> reservations = reservationService.getReservations(memberId);
        List<ReservationResponse> reservationResponses = ReservationResponse.fromAll(reservations);
        return ResponseEntity.ok().body(reservationResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest reservationCreateRequest,
            @LoginMember Long memberId) {
        Reservation savedReservation = reservationService.createReservation(
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
    public ResponseEntity<ReservationWaitResponse> createWait(@LoginMember Long memberId, @PathVariable Long reservationId) {
        return ResponseEntity.ok(ReservationWaitResponse.from(reservationService.createWait(memberId, reservationId)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest reservationUpdateRequest,
            @LoginMember Long memberId) {
        Reservation updatedReservation = reservationService.updateReservation(
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


}
