package roomescape.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.ReservationOrderResponse;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;

@RestController
public class ReservationMineController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationMineController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationOrderResponse>> readMine(@RequestParam("name") String name) {
        List<ReservationOrderResponse> responses = new ArrayList<>(reservationService.findByName(name));
        responses.addAll(waitingService.findByMemberName(name));
        responses.sort(Comparator.comparing(ReservationOrderResponse::date));

        return ResponseEntity.ok(responses);
    }
}
