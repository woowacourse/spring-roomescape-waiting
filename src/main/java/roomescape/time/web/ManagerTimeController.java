package roomescape.time.web;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.time.TimeService;
import roomescape.time.web.dto.TimeResponseDto;

@RestController
@RequestMapping("/manager/times")
public class ManagerTimeController {
    private final TimeService timeService;

    public ManagerTimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping
    public ResponseEntity<List<TimeResponseDto>> findAll() {
        List<TimeResponseDto> responses = timeService.findAll().stream()
                .map(TimeResponseDto::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
