package roomescape.web.controller.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationWaitingService;
import roomescape.web.controller.response.ReservationWaitingWebResponse;

@RestController
@RequestMapping("/admin/reservations/waiting")
public class AdminReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public AdminReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationWaitingWebResponse>> getAllReservationWaiting() {
        List<ReservationWaitingWebResponse> responses = reservationWaitingService.findAll()
                .stream()
                .map(ReservationWaitingWebResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationWaitingService.deleteBy(id);

        return ResponseEntity.noContent().build();
    }
}
