package roomescape.presentation.reservation;

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
import roomescape.application.reservation.ReservationTimeService;
import roomescape.application.reservation.dto.request.ReservationTimeRequest;
import roomescape.application.reservation.dto.response.AvailableTimeResponse;
import roomescape.application.reservation.dto.response.ReservationTimeResponse;
import roomescape.presentation.auth.PermissionRequired;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    @PermissionRequired
    public ResponseEntity<ReservationTimeResponse> create(@RequestBody @Valid ReservationTimeRequest request) {
        ReservationTimeResponse response = reservationTimeService.create(request);
        URI location = URI.create("/times/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        List<ReservationTimeResponse> responses = reservationTimeService.findAll();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @PermissionRequired
    public ResponseEntity<Void> delete(@PathVariable long id) {
        reservationTimeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableTimeResponse>> findAvailableTimes(@RequestParam LocalDate date,
                                                                          @RequestParam long themeId) {
        List<AvailableTimeResponse> responses = reservationTimeService.findAvailableTimes(date, themeId);
        return ResponseEntity.ok(responses);
    }
}
