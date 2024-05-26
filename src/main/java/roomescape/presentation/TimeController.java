package roomescape.presentation;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.TimeService;
import roomescape.application.dto.AvailableTimeResponse;
import roomescape.application.dto.TimeRequest;
import roomescape.application.dto.TimeResponse;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @PostMapping
    public ResponseEntity<TimeResponse> create(@RequestBody TimeRequest request) {
        TimeResponse response = timeService.create(request);
        URI location = URI.create("/times/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TimeResponse>> findAll() {
        List<TimeResponse> responses = timeService.findAll();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableTimeResponse>> findAvailableTimes(@RequestParam LocalDate date,
                                                                          @RequestParam Long themeId) {
        List<AvailableTimeResponse> responses = timeService.findAvailableTimes(date, themeId);
        return ResponseEntity.ok(responses);
    }
}
