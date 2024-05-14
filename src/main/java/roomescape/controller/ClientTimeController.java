package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.dto.TimeSlotResponses;
import roomescape.service.TimeService;

@RestController
@RequestMapping("/times")
public class ClientTimeController {
    private final TimeService timeService;

    public ClientTimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping
    public ResponseEntity<TimeSlotResponses> findAll() {
        return ResponseEntity.ok(timeService.findAll());
    }
}
