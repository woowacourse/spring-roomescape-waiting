package roomescape.time.controller;

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
import roomescape.time.dto.TimeRequest;
import roomescape.time.dto.TimeResponse;
import roomescape.time.facade.TimeFacadeService;

@RestController
@RequestMapping("/times")
public class TimeController {
    private final TimeFacadeService timeService;

    public TimeController(TimeFacadeService timeService) {
        this.timeService = timeService;
    }

    @PostMapping
    public ResponseEntity<TimeResponse> reservationTimeSave(
            @RequestBody TimeRequest timeRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(timeService.addTime(timeRequest));
    }

    @GetMapping
    public List<TimeResponse> reservationTimesList() {
        return timeService.findTimes();
    }

    @DeleteMapping("/{reservationTimeId}")
    public ResponseEntity<Void> reservationTimeRemove(@PathVariable long reservationTimeId) {
        timeService.removeTime(reservationTimeId);
        return ResponseEntity.noContent().build();
    }
}
