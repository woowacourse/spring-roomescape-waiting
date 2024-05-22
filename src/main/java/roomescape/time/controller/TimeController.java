package roomescape.time.controller;

import java.net.URI;
import java.util.List;
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
import roomescape.time.service.TimeService;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @PostMapping
    public ResponseEntity<TimeResponse> createReservationTime(@RequestBody TimeRequest timeRequest) {
        TimeResponse timeCreateResponse = timeService.addReservationTime(timeRequest);
        URI uri = URI.create("/times/" + timeCreateResponse.id());
        return ResponseEntity.created(uri)
                .body(timeCreateResponse);
    }

    @GetMapping
    public ResponseEntity<List<TimeResponse>> reservationTimesList() {
        List<TimeResponse> reservationReadResponse = timeService.findReservationTimes();
        return ResponseEntity.ok(reservationReadResponse);
    }

    @DeleteMapping("/{reservationTimeId}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable long reservationTimeId) {
        timeService.removeReservationTime(reservationTimeId);
        return ResponseEntity.noContent()
                .build();
    }

}
