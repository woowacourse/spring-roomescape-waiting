package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.BookingResponse;
import roomescape.controller.dto.ReservationPatchRequest;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.domain.Reservation;
import roomescape.service.SessionService;
import roomescape.service.dto.Booking;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final SessionService sessionService;

    public ReservationController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> reservations() {
        return ResponseEntity.ok(convertToResponses(sessionService.allReservations()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable long id) {
        return ResponseEntity.ok(ReservationResponse.from(sessionService.findReservationById(id)));
    }

    @GetMapping(params = "userName")
    public ResponseEntity<List<BookingResponse>> getReservationByName(@RequestParam String userName) {
        return ResponseEntity.ok(convertToBookingResponses(sessionService.findReservationByName(userName)));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody @Valid ReservationRequest request) {
        Reservation reservation = sessionService.makeReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId()))
                .body(ReservationResponse.from(reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable long id,
                                                  @RequestParam @NotBlank String userName) {
        sessionService.cancelReservation(id, userName);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable long id,
            @RequestParam @NotBlank String userName,
            @RequestBody @Valid ReservationRequest request) {
        return ResponseEntity.ok(ReservationResponse.from(sessionService.rescheduleReservation(id, userName, request)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> patchReservation(
            @PathVariable long id,
            @RequestParam @NotBlank String userName,
            @RequestBody ReservationPatchRequest request) {
        return ResponseEntity.ok(ReservationResponse.from(sessionService.patchReservation(id, userName, request)));
    }

    private List<ReservationResponse> convertToResponses(List<Reservation> reservations) {
        return reservations.stream().map(ReservationResponse::from).toList();
    }

    private List<BookingResponse> convertToBookingResponses(List<Booking> bookings) {
        return bookings.stream().map(BookingResponse::from).toList();
    }
}
