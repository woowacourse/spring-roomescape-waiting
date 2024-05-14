package roomescape.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.dto.TimeSlotRequest;
import roomescape.domain.dto.TimeSlotResponse;
import roomescape.service.TimeService;

import java.net.URI;

@RestController
@RequestMapping("/admin/times")
public class AdminTimeController {
    private final TimeService timeService;

    public AdminTimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @PostMapping
    public ResponseEntity<TimeSlotResponse> create(@RequestBody TimeSlotRequest timeSlotRequest) {
        TimeSlotResponse timeSlotResponse = timeService.create(timeSlotRequest);
        return ResponseEntity.created(URI.create("/admin/times/" + timeSlotResponse.id())).body(timeSlotResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
