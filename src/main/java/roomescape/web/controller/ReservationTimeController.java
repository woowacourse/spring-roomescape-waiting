package roomescape.web.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.reservationtime.TimeWithAvailableResponse;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.service.booking.time.ReservationTimeService;

@RestController
@RequestMapping("/times")
class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> addReservationTime(@RequestBody ReservationTimeRequest timeRequest) {
        Long savedId = reservationTimeService.resisterReservationTime(timeRequest);
        ReservationTimeResponse timeResponse = reservationTimeService.findReservationTime(savedId);
        return ResponseEntity.created(URI.create("/times/" + savedId)).body(timeResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getAllReservationTimes() {
        List<ReservationTimeResponse> timeResponses = reservationTimeService.findAllReservationTimes();
        return ResponseEntity.ok(timeResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationTimeResponse> getReservationTime(@PathVariable Long id) {
        ReservationTimeResponse timeResponse = reservationTimeService.findReservationTime(id);
        return ResponseEntity.ok(timeResponse);
    }

    @GetMapping("/available")
    public ResponseEntity<List<TimeWithAvailableResponse>> getAvailableTimes(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        List<TimeWithAvailableResponse> timesWithAvailable = reservationTimeService.getAvailableTimes(date, themeId);
        return ResponseEntity.ok(timesWithAvailable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.noContent().build();
    }
}
