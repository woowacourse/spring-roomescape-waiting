package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.AdminReservationResponse;
import roomescape.controller.dto.response.AdminReservationsResponse;
import roomescape.controller.dto.response.AdminWaitingResponse;
import roomescape.controller.dto.response.AdminWaitingsResponse;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;
import roomescape.service.ReservationWaitingService;
import roomescape.service.dto.Page;

@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {
    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    public AdminReservationController(ReservationService reservationService,
                                      ReservationWaitingService reservationWaitingService) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
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

    @GetMapping("/waiting")
    public ResponseEntity<AdminWaitingsResponse> getAllWaitings() {
        List<AdminWaitingResponse> responses = reservationWaitingService.findAllWaiting().stream()
                .map(AdminWaitingResponse::from)
                .toList();
        return ResponseEntity.ok(new AdminWaitingsResponse(responses));
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable long id) {
        reservationWaitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
