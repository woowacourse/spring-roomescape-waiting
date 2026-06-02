package roomescape.reservationtime.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationtime.dto.ReservationTimeResponseDTO;
import roomescape.reservationtime.service.ReservationTimeService;

@RestController
@RequestMapping("/api/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponseDTO>> readReservationTime() {
        return ResponseEntity.ok(reservationTimeService.findAllReservationTime());
    }

}
