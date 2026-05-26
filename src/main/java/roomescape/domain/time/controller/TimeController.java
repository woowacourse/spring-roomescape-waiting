package roomescape.domain.time.controller;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.time.dto.response.TimeResponseDto;
import roomescape.domain.time.service.TimeService;

@RestController
@RequestMapping("/api/times")
@Validated
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping
    public ResponseEntity<List<TimeResponseDto>> getAvailableTimes(@RequestParam LocalDate date,
        @RequestParam @Positive(message = "themeId의 값은 양수여야 합니다.") Long themeId) {
        return ResponseEntity.ok(timeService.getAvailableTimes(date, themeId));
    }
}
