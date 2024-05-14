package roomescape.time.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.net.URI;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.time.dto.AvailableTimeResponse;
import roomescape.time.dto.TimeCreateRequest;
import roomescape.time.dto.TimeResponse;
import roomescape.time.service.TimeService;

@RestController
@RequestMapping("/times")
public class TimeController {
    private final TimeService service;

    public TimeController(TimeService service) {
        this.service = service;
    }

    @GetMapping
    public List<TimeResponse> findTimes() {
        return service.findTimes();
    }

    @GetMapping("/available")
    public List<AvailableTimeResponse> findAvailableTimes(
            @RequestParam @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Long themeId) {
        return service.findAvailableTimes(date, themeId);
    }

    @PostMapping
    public ResponseEntity<TimeResponse> createTime(@RequestBody TimeCreateRequest request) {
        TimeResponse response = service.createTime(request);

        URI location = URI.create("/times/" + response.id());
        return ResponseEntity
                .created(location)
                .body(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTime(@PathVariable Long id) {
        service.deleteTime(id);
    }
}

