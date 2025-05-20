package roomescape.admin.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.dto.request.AdminReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationCondition;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ReservationService reservationService;

    public AdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody AdminReservationCreateRequest request) {
        ReservationResponse reservationResponse = reservationService.createReservation(
                request.memberId(), request.timeId(), request.themeId(), request.date()
        );
        return ResponseEntity
                .created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> getReservations(@ModelAttribute ReservationCondition cond) {
        return reservationService.findReservations(cond);
    }
}
