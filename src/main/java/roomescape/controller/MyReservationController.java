package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.MyReservationResponse;
import roomescape.controller.dto.response.MyReservationsResponse;
import roomescape.service.ReservationService;

@RequestMapping("/reservations-mine")
@RestController
public class MyReservationController {
    private final ReservationService reservationService;

    public MyReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<MyReservationsResponse> getMyReservations(@RequestParam String username) {
        List<MyReservationResponse> responses = reservationService.findAllMine(username)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
        return ResponseEntity.ok(new MyReservationsResponse(responses));
    }
}
