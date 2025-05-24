package roomescape.booking.reservation;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.booking.reservation.dto.AdminFilterReservationRequest;
import roomescape.booking.reservation.dto.AdminReservationRequest;
import roomescape.booking.reservation.dto.ReservationResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
@AllArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid final AdminReservationRequest request
    ) {
        final ReservationResponse response = reservationService.createForAdmin(request);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> readAllByMemberAndThemeAndDateRange(
            @ModelAttribute final AdminFilterReservationRequest request
    ) {
        final List<ReservationResponse> response = reservationService
                .readAllByMemberAndThemeAndDateRange(request);
        return ResponseEntity.ok(response);
    }
}
