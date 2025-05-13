package roomescape.presentation.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.TimeService;
import roomescape.application.dto.TimeCreateDto;
import roomescape.application.dto.TimeDto;
import roomescape.domain.repository.dto.TimeDataWithBookingInfo;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeService service;

    public TimeController(TimeService service) {
        this.service = service;
    }

    @GetMapping
    public List<TimeDto> getAllTimes() {
        return service.getAllTimes();
    }

    @PostMapping
    public ResponseEntity<TimeDto> addTime(@Valid @RequestBody TimeCreateDto request) {
        TimeDto timeDto = service.registerNewTime(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(timeDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable(name = "id") Long id) {
        service.deleteTime(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/booking-status")
    public ResponseEntity<List<TimeDataWithBookingInfo>> getTimesWithBookingInfo(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        List<TimeDataWithBookingInfo> timesWithBookingInfo = service.getTimesWithBookingInfo(date, themeId);
        return ResponseEntity.ok().body(timesWithBookingInfo);
    }
}
