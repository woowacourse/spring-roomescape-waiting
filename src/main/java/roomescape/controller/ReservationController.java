package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UpdateReservationRequest;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationPaymentResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationService;

import java.net.URI;
import java.time.LocalDateTime;
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
    public ResponseEntity<ReservationPaymentResponse> addReservation(
            @Valid @RequestBody ReservationRequest request) {
        CreateReservationCommand command = new CreateReservationCommand(
                request.name(), request.date(), request.timeId(), request.themeId()
        );
        ReservationPaymentResponse response = reservationService.addReservation(command, LocalDateTime.now());
        return ResponseEntity.created(URI.create(LOCATION_DEFAULT_VALUE + response.reservationId()))
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
            @Valid @RequestBody UpdateReservationRequest request) {
        UpdateReservationCommand command = new UpdateReservationCommand(
                request.date(), request.timeId()
        );
        ReservationResponse response = reservationService.update(reservationId, command, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("reservationId") Long reservationId) {
        reservationService.cancel(reservationId, LocalDateTime.now());
        return ResponseEntity.noContent().build();
    }
}
