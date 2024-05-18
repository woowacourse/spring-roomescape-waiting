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

    @GetMapping
    public ResponseEntity<List<TimeResponse>> findTimes() {
        List<TimeResponse> reservationReadResponse = timeService.findReservationTimes();
        return ResponseEntity.ok(reservationReadResponse);
    }

    @PostMapping
    public ResponseEntity<TimeResponse> createTime(@RequestBody TimeRequest request) {
        TimeResponse timeCreateResponse = timeService.addReservationTime(request);
        URI uri = URI.create("/times/" + timeCreateResponse.id());
        return ResponseEntity.created(uri)
                .body(timeCreateResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable long id) {
        timeService.removeReservationTime(id);
        return ResponseEntity.noContent()
                .build();
    }
}
