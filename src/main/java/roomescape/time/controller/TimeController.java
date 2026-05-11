package roomescape.time.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.time.service.TimeService;
import roomescape.time.dto.AvailableTimeResponse;
import roomescape.time.dto.TimeResponse;

@RestController
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping("/times/reservation-time")
    public ResponseEntity<List<TimeResponse>> readAll() {
        List<TimeResponse> reservationTimes = timeService.findAll().stream()
                .map(TimeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(reservationTimes);
    }

    @GetMapping(value = "/times/available-time", params = {"themeId", "date"})
    public ResponseEntity<List<AvailableTimeResponse>> readByThemeIdAndDate(@RequestParam Long themeId,
                                                                            @RequestParam LocalDate date) {
        List<AvailableTimeResponse> availableTimes = timeService.findByThemeIdAndDate(themeId, date).stream()
                .map(AvailableTimeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(availableTimes);
    }
}
