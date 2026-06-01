package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.AdminReservationResponse;
import roomescape.controller.dto.response.AdminReservationsResponse;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;
import roomescape.service.dto.Page;

@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {
    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<AdminReservationsResponse> getAllReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Reservation> result = reservationService.findAllWithCount(page, size);
        List<AdminReservationResponse> responses = result.content().stream()
                .map(r -> AdminReservationResponse.from(r, r.getTheme()))
                .toList();
        return ResponseEntity.ok(new AdminReservationsResponse(responses, result.totalCount(), page, size));
    }
}
