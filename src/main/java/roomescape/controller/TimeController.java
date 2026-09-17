package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.TimeRequest;
import roomescape.controller.dto.TimeResponse;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.service.TimeService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping
    public ResponseEntity<List<TimeResponse>> getTimes() {
        return ResponseEntity.ok(convertToTimeResponses(timeService.allTimes()));
    }

    @GetMapping(params = {"themeId", "date"})
    public ResponseEntity<List<TimeResponse>> getAvailableTimes(long themeId, LocalDate date) {
        return ResponseEntity.ok(convertToTimeResponsesFromThemeSlots(timeService.findThemeSlotBy(themeId, date)));
    }

    @PostMapping
    public ResponseEntity<TimeResponse> createTime(@Valid @RequestBody TimeRequest timeRequest) {
        Time time = timeService.saveTime(timeRequest.startAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(TimeResponse.from(time));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable long id) {
        timeService.removeTime(id);
        return ResponseEntity.noContent().build();
    }

    private List<TimeResponse> convertToTimeResponses(List<Time> reservationTimes) {
        return reservationTimes.stream()
                .map(TimeResponse::from)
                .toList();
    }

    private List<TimeResponse> convertToTimeResponsesFromThemeSlots(List<ThemeSlot> themeSlots) {
        return themeSlots.stream()
                .map(TimeResponse::from)
                .toList();
    }
}
