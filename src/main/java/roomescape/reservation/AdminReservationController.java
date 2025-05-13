package roomescape.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.AdminFilterReservationRequest;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.ReservationResponse;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    @Autowired
    public AdminReservationController(
            final ReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }

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
