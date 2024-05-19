package roomescape.reservation.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.dto.TimeCreateRequest;
import roomescape.reservation.dto.TimeResponse;
import roomescape.reservation.service.ReservationTimeService;

@RestController
public class ReservationTimeApiController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeApiController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/times")
    public ResponseEntity<List<TimeResponse>> findAll() {
        List<TimeResponse> times = reservationTimeService.findAll();

        return ResponseEntity.ok(times);
    }

    @GetMapping("/times/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAvailableTimes(
            @RequestParam("date") LocalDate date,
            @RequestParam("theme-id") Long themeId
    ) {
        List<AvailableReservationTimeResponse> availableTimes = reservationTimeService.findAvailableTimes(date,
                themeId);

        return ResponseEntity.ok(availableTimes);
    }

    @PostMapping("/times")
    public ResponseEntity<TimeResponse> save(@Valid @RequestBody TimeCreateRequest timeCreateRequest) {
        Long saveId = reservationTimeService.save(timeCreateRequest);
        TimeResponse timeResponse = reservationTimeService.findById(saveId);

        return ResponseEntity.created(URI.create("/times/" + saveId)).body(timeResponse);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        reservationTimeService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
