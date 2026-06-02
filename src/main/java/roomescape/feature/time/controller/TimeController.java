package roomescape.feature.time.controller;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.feature.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.feature.time.service.TimeService;

@RestController
@RequestMapping("/api/times")
@Validated
@RequiredArgsConstructor
public class TimeController {

    private final TimeService timeService;

    @GetMapping
    public ResponseEntity<List<TimeAvailabilityResponseDto>> getAvailableTimes(@RequestParam LocalDate date,
        @RequestParam @Positive(message = "themeId의 값은 양수여야 합니다.") Long themeId) {
        return ResponseEntity.ok(timeService.getTimeAvailabilities(date, themeId));
    }
}
