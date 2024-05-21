package roomescape.controller.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.reservation.ReservationTime;
import roomescape.dto.reservation.AvailableReservationTimeResponse;
import roomescape.dto.reservation.AvailableReservationTimeSearch;
import roomescape.service.ReservationTimeService;
import roomescape.dto.reservation.ReservationTimeResponse;
import roomescape.dto.reservation.ReservationTimeSaveRequest;

import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(@RequestBody final ReservationTimeSaveRequest request) {
        final ReservationTime reservationTime = request.toModel();
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationTimeService.create(reservationTime));
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findReservationTimes() {
        return ResponseEntity.ok(reservationTimeService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable final Long id) {
        reservationTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAvailableReservationTimes(
            @ModelAttribute final AvailableReservationTimeSearch availableReservationTimeSearch) {
        return ResponseEntity.ok(reservationTimeService.findAvailableReservationTimes(availableReservationTimeSearch));
    }
}
