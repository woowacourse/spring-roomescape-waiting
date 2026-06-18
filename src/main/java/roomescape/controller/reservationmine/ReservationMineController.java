package roomescape.controller.reservationmine;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.service.reservationmine.ReservationMineService;

@RestController
@RequestMapping("/reservations-mine")
public class ReservationMineController {

    private final ReservationMineService reservationMineService;

    public ReservationMineController(final ReservationMineService reservationMineService) {
        this.reservationMineService = reservationMineService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservationsMine(
            @RequestParam(required = false) final String name
    ) {
        return ResponseEntity.ok(reservationMineService.getAllByName(name));
    }
}
