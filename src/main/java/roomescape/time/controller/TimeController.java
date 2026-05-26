package roomescape.time.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import roomescape.theme.service.ThemeService;
import roomescape.time.controller.dto.TimeResponseDto;
import roomescape.time.controller.dto.TimeSaveRequestDto;
import roomescape.time.service.TimeService;

@RestController
public class TimeController {
    private final TimeService timeService;
    private final ThemeService themeService;

    public TimeController(TimeService timeService, ThemeService themeService) {
        this.timeService = timeService;
        this.themeService = themeService;
    }

    @PostMapping("/times")
    public ResponseEntity<TimeResponseDto> create(@RequestBody @Valid TimeSaveRequestDto request) {
        TimeResponseDto body = TimeResponseDto.from(timeService.create(request.startAt(), request.endAt()));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/times")
    public ResponseEntity<List<TimeResponseDto>> findAll() {
        List<TimeResponseDto> body = timeService.findAll()
                .stream()
                .map(TimeResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/times", params = {"themeId", "date"})
    public ResponseEntity<List<TimeResponseDto>> getAvailableTimes(
            @RequestParam Long themeId,
            @RequestParam LocalDate date
    ) {
        List<TimeResponseDto> body = themeService.getAvailableTimes(themeId, date).stream()
                .map(TimeResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        timeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
