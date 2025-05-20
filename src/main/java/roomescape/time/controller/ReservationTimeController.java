package roomescape.time.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.time.dto.CreateReservationTimeRequest;
import roomescape.time.dto.ReservationTimeResponse;
import roomescape.time.dto.TimeAvailabilityResponse;
import roomescape.time.service.ReservationAvailabilityService;
import roomescape.time.service.ReservationTimeService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;
    private final ReservationAvailabilityService reservationAvailabilityService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService,
                                     final ReservationAvailabilityService reservationAvailabilityService) {
        this.reservationTimeService = reservationTimeService;
        this.reservationAvailabilityService = reservationAvailabilityService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(@RequestBody @Valid final CreateReservationTimeRequest request) {
        final ReservationTimeResponse response = reservationTimeService.createReservationTime(request);
        return ResponseEntity.created(URI.create("/times/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        final List<ReservationTimeResponse> responses = reservationTimeService.getReservationTimes();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<TimeAvailabilityResponse>> findAvailabilityTimes(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") long themeId
    ) {
        final List<TimeAvailabilityResponse> responses = reservationAvailabilityService.getAllTimeAvailability(date, themeId);
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        reservationTimeService.deleteReservationTimeById(id);
        return ResponseEntity.noContent().build();
    }
}
