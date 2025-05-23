package roomescape.reservationtime;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;

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
                .findAllAvailable(themeId, date);
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
