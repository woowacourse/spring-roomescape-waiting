package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.request.ReservationTimeRequest;
import roomescape.controller.response.IsReservedTimeResponse;
import roomescape.controller.response.ReservationTimeResponse;
import roomescape.model.ReservationTime;
import roomescape.service.ReservationTimeService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/times")
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeService.findAllReservationTimes();
        List<ReservationTimeResponse> responses = reservationTimes.stream()
                .map(time -> new ReservationTimeResponse(time.getId(), time.getStartAt()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/times")
    public ResponseEntity<ReservationTime> createReservationTime(@RequestBody ReservationTimeRequest request) {
        ReservationTime reservationTime = reservationTimeService.addReservationTime(request);
        return ResponseEntity.created(URI.create("/times/" + reservationTime.getId())).body(reservationTime);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable("id") long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/times/reserved")
    public ResponseEntity<List<IsReservedTimeResponse>> getPossibleReservationTimes(
            @RequestParam(name = "date") LocalDate date,
            @RequestParam(name = "themeId") long themeId) {
        List<IsReservedTimeResponse> response = reservationTimeService.getIsReservedTime(date, themeId);
        return ResponseEntity.ok(response);
    }
}
