package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.*;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        return ResponseEntity.ok(convertToReservationResponse(reservationService.allReservations()));
    }

    @GetMapping(params = "name")
    public ResponseEntity<MyReservationResponse> getMyReservations(@NotBlank @RequestParam String name) {
        return ResponseEntity.ok(reservationService.findReservationBy(name));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest reservationRequest) {
        Reservation reservation = reservationService.saveReservation(
                reservationRequest.name(),
                reservationRequest.themeSlotId()
        );
        ReservationResponse reservationResponse = toResponse(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationResponse);
    }

    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelMyReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> modifyMyReservation(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationModifyRequest request
    ) {
        Reservation reservation = reservationService.modifyReservation(reservationId, request.themeSlotId());
        return ResponseEntity.ok().body(toResponse(reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable long id) {
        reservationService.removeReservation(id);
        return ResponseEntity.noContent().build();
    }

    private List<ReservationResponse> convertToReservationResponse(List<Reservation> reservations) {
        return reservations.stream()
                .map(this::toResponse)
                .toList();
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.from(reservation);
    }
}
