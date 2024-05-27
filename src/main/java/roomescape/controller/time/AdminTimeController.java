package roomescape.controller.time;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.time.dto.AvailabilityTimeResponse;
import roomescape.controller.time.dto.CreateTimeRequest;
import roomescape.service.TimeService;

@RestController
@RequestMapping("/admin/times")
public class AdminTimeController {

    private final TimeService timeService;

    public AdminTimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @PostMapping
    public ResponseEntity<AvailabilityTimeResponse> addTime(
            @RequestBody @Valid final CreateTimeRequest createTimeRequest) {
        final AvailabilityTimeResponse time = timeService.addTime(createTimeRequest);
        final URI uri = UriComponentsBuilder.fromPath("/admin/times/{id}")
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
