package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Reservation;
import roomescape.dto.command.CancelReservationCommand;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.request.CreatePaymentReservationRequest;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.response.ReservationPaymentResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationWithStatusResponses;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.dto.request.UpdateReservationRequest;
import roomescape.infrastructure.LoginUserId;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/mine")
    public ResponseEntity<ReservationWithStatusResponses> readMyReservations(@LoginUserId Long userId) {
        return ResponseEntity.ok(reservationService.getMyReservations(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> readReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(ReservationResponse.from(reservationService.getReservation(id)));
    }

    @PostMapping
    public ResponseEntity<ReservationPaymentResponse> createReservation(
            @LoginUserId Long userId,
            @Valid @RequestBody CreatePaymentReservationRequest request) {
        ReservationPaymentResponse createdReservation = reservationService.createReservation(
                CreateReservationCommand.of(userId, request));

        URI location = URI.create("/reservations/" + createdReservation.reservationId());
        return ResponseEntity.created(location).body(createdReservation);
    }

    @PostMapping("/waiting")
    public ResponseEntity<Void> createWaitingReservation(
            @LoginUserId Long userId,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        Reservation createdReservationWaiting = reservationService.createWaitingReservation(
                CreateReservationCommand.of(userId, request));

        URI location = URI.create("/reservations/" + createdReservationWaiting.getId());
        return ResponseEntity.created(location).build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @LoginUserId Long userId,
            @Valid @RequestBody UpdateReservationRequest request) {
        Reservation updated = reservationService.updateOwnReservation(
                UpdateReservationCommand.of(id, userId, request));
        return ResponseEntity.ok(ReservationResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @LoginUserId Long userId) {
        reservationService.cancelOwnReservation(new CancelReservationCommand(id, userId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> cancelWaitingReservation(
            @PathVariable Long id,
            @LoginUserId Long userId) {
        reservationService.cancelOwnWaitingReservation(new CancelReservationCommand(id, userId));
        return ResponseEntity.noContent().build();
    }
}
