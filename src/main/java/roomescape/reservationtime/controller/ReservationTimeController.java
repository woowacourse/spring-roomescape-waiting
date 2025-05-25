package roomescape.reservationtime.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponseWithBookedStatus;
import roomescape.reservationtime.service.ReservationTimeServiceFacade;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {
    private final ReservationTimeServiceFacade reservationTimeService;

    @Autowired
    public ReservationTimeController(ReservationTimeServiceFacade reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(
            @RequestBody ReservationTimeCreateRequest reservationTimeCreateRequest) {
        ReservationTimeResponse createdReservationTime = reservationTimeService.createReservationTime(
                reservationTimeCreateRequest);
        URI location = URI.create("/times/" + createdReservationTime.id());
        return ResponseEntity.created(location).body(createdReservationTime);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.findAll();
        return ResponseEntity.ok(reservationTimes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationTimeService.deleteReservationTimeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationTimeResponseWithBookedStatus>> searchReservationTimes(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Long themeId) {
        List<ReservationTimeResponseWithBookedStatus> response = reservationTimeService.findAvailableReservationTimesByDateAndThemeId(
                date, themeId);
        return ResponseEntity.ok(response);
    }
}
