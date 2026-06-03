package roomescape.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ReservationTimeResponses;
import roomescape.service.ReservationTimeService;

@Validated
@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<ReservationTimeResponses> readTimes(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(reservationTimeService.getReservationTimes(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationTimeResponse> readTime(@PathVariable Long id) {
        return ResponseEntity.ok(ReservationTimeResponse.from(reservationTimeService.getReservationTime(id)));
    }
}
