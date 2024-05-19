package roomescape.reservationtime.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationtime.dto.TimeRequest;
import roomescape.reservationtime.dto.TimeResponse;
import roomescape.reservationtime.service.TimeService;

@RestController
@RequestMapping("/times")
public class TimeController {
    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @PostMapping
    public ResponseEntity<TimeResponse> reservationTimeSave(
            @RequestBody TimeRequest timeRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(timeService.addReservationTime(timeRequest));
    }

    @GetMapping
    public List<TimeResponse> reservationTimesList() {
        return timeService.findReservationTimes();
    }

    @DeleteMapping("/{reservationTimeId}")
    public ResponseEntity<Void> reservationTimeRemove(@PathVariable long reservationTimeId) {
        timeService.removeReservationTime(reservationTimeId);
        return ResponseEntity.noContent().build();
    }
}
