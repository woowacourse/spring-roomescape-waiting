package roomescape.controller.time;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.time.dto.AvailabilityTimeRequest;
import roomescape.controller.time.dto.AvailabilityTimeResponse;
import roomescape.controller.time.dto.CreateTimeRequest;
import roomescape.controller.time.dto.ReadTimeResponse;
import roomescape.service.TimeService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeService timeService;

    public TimeController(final TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping
    public List<ReadTimeResponse> getTimes() {
        return timeService.getTimes();
    }

    @GetMapping(value = "/availability", params = {"date", "themeId"})
    public List<AvailabilityTimeResponse> getAvailableTimes(
            @Valid final AvailabilityTimeRequest availabilityTimeRequest) {
        return timeService.getAvailableTimes(availabilityTimeRequest);
    }

    @PostMapping
    public ResponseEntity<AvailabilityTimeResponse> addTime(
            @RequestBody @Valid final CreateTimeRequest createTimeRequest) {
        final AvailabilityTimeResponse time = timeService.addTime(createTimeRequest);
        final URI uri = UriComponentsBuilder.fromPath("/reservations/{id}")
                .buildAndExpand(time.id())
                .toUri();

        return ResponseEntity.created(uri)
                .body(time);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable("id") final long id) {
        timeService.deleteTime(id);
        return ResponseEntity.noContent()
                .build();
    }
}
