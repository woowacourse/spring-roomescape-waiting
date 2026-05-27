package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.dto.command.CreateReservationCommand;
import roomescape.reservation.dto.command.UpdateReservationCommand;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.UpdateReservationRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private static final String LOCATION_DEFAULT_VALUE = "/reservations/";

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @Valid @RequestBody ReservationRequest request) {
        CreateReservationCommand command = new CreateReservationCommand(
                request.name(), request.date(), request.timeId(), request.themeId()
        );
        ReservationResponse response = reservationService.addReservation(command);
        return ResponseEntity.created(URI.create(LOCATION_DEFAULT_VALUE + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<MyReservationResponse>> getMyReservations(@RequestParam(name = "name") String name) {
        List<MyReservationResponse> responses = reservationService.getMyReservations(name);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable("reservationId") Long reservationId,
            @RequestBody UpdateReservationRequest request) {
        UpdateReservationCommand command = new UpdateReservationCommand(
                request.date(), request.timeId()
        );
        ReservationResponse response = reservationService.update(reservationId, command);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("reservationId") Long reservationId) {
        reservationService.delete(reservationId);
        return ResponseEntity.noContent().build();
    }
}
