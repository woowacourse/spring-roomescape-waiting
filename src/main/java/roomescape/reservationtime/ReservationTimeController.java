package roomescape.reservationtime;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
@AllArgsConstructor
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(
            @RequestBody @Valid final ReservationTimeRequest request
    ) {
        final ReservationTimeResponse response = reservationTimeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        final List<ReservationTimeResponse> response = reservationTimeService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-time")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAllAvailableTimes(
            @RequestParam("themeId") final Long themeId,
            @RequestParam("date") final LocalDate date
    ) {
        final List<AvailableReservationTimeResponse> response = reservationTimeService
                .findAllAvailableTimes(themeId, date);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable("id") final Long id
    ) {
        reservationTimeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
