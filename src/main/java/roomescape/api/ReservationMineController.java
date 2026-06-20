package roomescape.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.ReservationWithStatusResponse;
import roomescape.service.ReservationService;

@RestController
public class ReservationMineController {

    private final ReservationService reservationService;

    public ReservationMineController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationWithStatusResponse>> findMine(@RequestParam String name) {
        List<ReservationWithStatusResponse> responses = reservationService.getMyReservations(name).stream()
            .map(ReservationWithStatusResponse::from)
            .toList();
        return ResponseEntity.ok(responses);
    }
}
