package roomescape.booking.reservation;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.AuthenticationPrincipal;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.BookingService;
import roomescape.booking.reservation.dto.ReservationRequest;
import roomescape.booking.reservation.dto.ReservationResponse;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@AllArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationCreateService reservationCreateService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid final ReservationRequest request,
            @AuthenticationPrincipal final LoginMember member
    ) {
        final ReservationResponse response = reservationCreateService.create(request, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> readAll() {
        final List<ReservationResponse> response = reservationService.readAll();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable("id") final Long id
    ) {
        bookingService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
