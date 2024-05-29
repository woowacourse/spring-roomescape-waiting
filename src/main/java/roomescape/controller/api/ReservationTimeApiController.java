package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.api.dto.request.ReservationTimeRequest;
import roomescape.controller.api.dto.response.AvailableReservationTimesResponse;
import roomescape.controller.api.dto.response.ReservationTimeResponse;
import roomescape.controller.api.dto.response.ReservationTimesResponse;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.input.AvailableReservationTimeInput;
import roomescape.service.dto.output.AvailableReservationTimeOutput;
import roomescape.service.dto.output.ReservationTimeOutput;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeApiController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeApiController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(@RequestBody final ReservationTimeRequest request) {
        final ReservationTimeOutput output = reservationTimeService.createReservationTime(request.toInput());
        return ResponseEntity.created(URI.create("/times/" + output.id()))
                .body(ReservationTimeResponse.from(output));
    }

    @GetMapping
    public ResponseEntity<ReservationTimesResponse> getAllReservationTimes() {
        final List<ReservationTimeOutput> output = reservationTimeService.getAllReservationTimes();
        return ResponseEntity.ok(ReservationTimesResponse.from(output));
    }

    @GetMapping("/available")
    public ResponseEntity<AvailableReservationTimesResponse> getAllReservationTimes(
            @RequestParam final LocalDate date,
            @RequestParam final Long themeId) {
        final List<AvailableReservationTimeOutput> response = reservationTimeService.getAvailableTimes(
                new AvailableReservationTimeInput(themeId, date));
        return ResponseEntity.ok(AvailableReservationTimesResponse.from(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable final long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.noContent()
                .build();
    }
}
