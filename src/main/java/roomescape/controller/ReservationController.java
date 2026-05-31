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
import roomescape.dto.reservation.command.CancelReservationCommand;
import roomescape.dto.reservation.command.CreateReservationCommand;
import roomescape.dto.reservation.request.CreateReservationRequest;
import roomescape.dto.reservation.response.ReservationResponse;
import roomescape.dto.reservation.response.ReservationWithStatusResponses;
import roomescape.dto.reservation.command.UpdateReservationCommand;
import roomescape.dto.reservation.request.UpdateReservationRequest;
import roomescape.dto.reservation.response.WaitingReservationResponse;
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
    public ResponseEntity<ReservationResponse> createReservation(
            @LoginUserId Long userId,
            @Valid @RequestBody CreateReservationRequest request) {
        Reservation createdReservation = reservationService.createReservation(
                CreateReservationCommand.of(userId, request));

        URI location = URI.create("/reservations/" + createdReservation.getId());
        return ResponseEntity.created(location).body(ReservationResponse.from(createdReservation));
    }

    @PostMapping("/waiting")
    public ResponseEntity<WaitingReservationResponse> createWaitingReservation(
            @LoginUserId Long userId,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        WaitingReservationResponse response = reservationService.createWaitingReservation(
                CreateReservationCommand.of(userId, request));

        URI location = URI.create("/reservations/" + response.id());
        return ResponseEntity.created(location).body(response);
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
        reservationService.cancelOwnReservation(CancelReservationCommand.of(id, userId));
        return ResponseEntity.ok().build();
    }
}
