package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.dto.ReservationTimeRequest;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.service.ReservationTimeService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> addTime(
            @RequestBody ReservationTimeRequest reservationTimeRequest) {
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.addTime(reservationTimeRequest);
        return ResponseEntity.created(URI.create("/times/" + reservationTimeResponse.id()))
                .body(reservationTimeResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findTimes() {
        return ResponseEntity.ok(reservationTimeService.findTimes());
    }

    @GetMapping("/available")
    public ResponseEntity<List<ReservationTimeResponse>> findTimesWithAlreadyBooked(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        return ResponseEntity.ok(reservationTimeService.findTimesWithAlreadyBooked(date, themeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationTimeResponse> getTime(@PathVariable Long id) {
        return ResponseEntity.ok(reservationTimeService.getTime(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable Long id) {
        reservationTimeService.deleteTime(id);
        return ResponseEntity.noContent().build();
    }
}
