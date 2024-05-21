package roomescape.controller.api;

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
import roomescape.controller.api.dto.request.ReservationTimeRequest;
import roomescape.controller.api.dto.response.AvailableReservationTimesResponse;
import roomescape.controller.api.dto.response.ReservationTimeResponse;
import roomescape.controller.api.dto.response.ReservationTimesResponse;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.input.AvailableReservationTimeInput;
import roomescape.service.dto.output.AvailableReservationTimeOutput;
import roomescape.service.dto.output.ReservationTimeOutput;

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
                .body(ReservationTimeResponse.toResponse(output));
    }

    @GetMapping
    public ResponseEntity<ReservationTimesResponse> getAllReservationTimes() {
        final List<ReservationTimeOutput> output = reservationTimeService.getAllReservationTimes();
        return ResponseEntity.ok(ReservationTimesResponse.toResponse(output));
    }

    @GetMapping("/available")
    public ResponseEntity<AvailableReservationTimesResponse> getAllReservationTimes(
            @RequestParam final LocalDate date,
            @RequestParam final Long themeId) {
        final List<AvailableReservationTimeOutput> response = reservationTimeService.getAvailableTimes(
                new AvailableReservationTimeInput(themeId, date));
        return ResponseEntity.ok(AvailableReservationTimesResponse.toResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable final long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.noContent()
                .build();
    }
}
