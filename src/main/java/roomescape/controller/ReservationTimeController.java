package roomescape.controller;

import jakarta.validation.Valid;
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
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.request.ReservationAvailabilityTimeRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.response.ReservationAvailabilityTimeResponse;
import roomescape.service.dto.response.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAllTimes() {
        return ResponseEntity.ok(reservationTimeService.findAll());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ReservationAvailabilityTimeResponse>> findReservationTimesWithBookStatus(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") Long themeId) {
        ReservationAvailabilityTimeRequest timeRequest = new ReservationAvailabilityTimeRequest(date, themeId);
        return ResponseEntity.ok(reservationTimeService.findReservationAvailabilityTimes(timeRequest));
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> addTime(
            @Valid @RequestBody ReservationTimeRequest reservationTimeRequest) {
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.save(reservationTimeRequest);
        return ResponseEntity.created(URI.create("/admin/time"))
                .body(reservationTimeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable Long id) {
        reservationTimeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
