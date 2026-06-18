package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.ReservationOrderResponse;
import roomescape.service.ReservationService;

@RestController
public class ReservationMineController {

    private final ReservationService reservationService;

    public ReservationMineController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationOrderResponse>> readMine(@RequestParam("name") String name) {
        List<ReservationOrderResponse> responses = reservationService.findByName(name);
        return ResponseEntity.ok(responses);
    }
}
