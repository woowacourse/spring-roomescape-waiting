package roomescape.controller.api;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.TimeSlotResponse;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/api/times")
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> read() {
        List<ReservationTimeResponse> responses = reservationTimeService.findAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<TimeSlotResponse>> getAvailableTime(
            @RequestParam long themeId,
            @RequestParam LocalDate date
    ) {
        List<TimeSlotResponse> availableTimes = reservationTimeService.findAvailableTime(themeId, date);

        return ResponseEntity.ok().body(availableTimes);
    }
}
