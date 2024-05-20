package roomescape.reservationtime.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> reservationTimeSave(
            @RequestBody ReservationTimeRequest reservationTimeRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationTimeService.addReservationTime(reservationTimeRequest));
    }

    @GetMapping
    public List<ReservationTimeResponse> reservationTimesList() {
        return reservationTimeService.findReservationTimes();
    }

    @DeleteMapping("/{reservationTimeId}")
    public ResponseEntity<Void> reservationTimeRemove(@PathVariable long reservationTimeId) {
        reservationTimeService.removeReservationTime(reservationTimeId);
        return ResponseEntity.noContent().build();
    }
}
