package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.request.CreateReservationTimeRequest;
import roomescape.controller.response.AvailableReservationTimeResponse;
import roomescape.controller.response.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;
import roomescape.service.result.AvailableReservationTimeResult;
import roomescape.service.result.ReservationTimeResult;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationService;

    public ReservationTimeController(ReservationTimeService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(
            @Valid @RequestBody CreateReservationTimeRequest createReservationTImeRequest) {

        ReservationTimeResult reservationTimeResult = reservationService.create(createReservationTImeRequest.toServiceParam());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationTimeResponse.from(reservationTimeResult));
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        List<ReservationTimeResult> reservationTimeResults = reservationService.findAll();
        List<ReservationTimeResponse> reservationTimeResponses = reservationTimeResults.stream()
                .map(ReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(reservationTimeResponses);
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAvailableTimes(@RequestParam Long themeId, @RequestParam("date") LocalDate reservationDate) {
        List<AvailableReservationTimeResult> availableTimes = reservationService.findAvailableTimesByThemeIdAndDate(themeId, reservationDate);
        List<AvailableReservationTimeResponse> availableReservationTimeResponses = availableTimes.stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(availableReservationTimeResponses);
    }

    @DeleteMapping("/{reservationTimeId}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable("reservationTimeId") Long reservationTimeId) {
        reservationService.deleteById(reservationTimeId);
        return ResponseEntity.noContent().build();
    }
}
