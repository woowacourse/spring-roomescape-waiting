package roomescape.schedule;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.schedule.dto.ScheduleRequest;
import roomescape.schedule.dto.ScheduleResponse;

@RestController
@RequestMapping("/schedules")
@AllArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleResponse> create(@RequestBody @Valid final ScheduleRequest request) {
        ScheduleResponse response = scheduleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
