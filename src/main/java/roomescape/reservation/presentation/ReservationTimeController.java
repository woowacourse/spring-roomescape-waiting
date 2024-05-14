package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.dto.request.ReservationTimeSaveRequest;
import roomescape.reservation.application.ReservationTimeService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(
            @RequestBody @Valid ReservationTimeSaveRequest request) {
        ReservationTime newReservationTime = request.toModel();
        ReservationTime createReservationTime = reservationTimeService.create(newReservationTime);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationTimeResponse.from(createReservationTime));
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeService.findAll();
        return ResponseEntity.ok(reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long id) {
        reservationTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAllByDateAndThemeId(
            @RequestParam LocalDate date, @RequestParam Long themeId) {
        return ResponseEntity.ok(reservationTimeService.findAvailableReservationTimes(date, themeId));
    }
}
