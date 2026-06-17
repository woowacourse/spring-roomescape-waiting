package roomescape.time.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.time.Time;
import roomescape.time.web.TimeRequestDto;
import roomescape.time.web.TimeResponseDto;
import roomescape.time.TimeService;

@RestController
@RequestMapping("/admin/times")
public class AdminTimeController {
    private final TimeService timeService;

    public AdminTimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @PostMapping
    public ResponseEntity<TimeResponseDto> create(@Valid @RequestBody TimeRequestDto timeRequest) {
        Time time = timeService.create(timeRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .build(time.getId());
        return ResponseEntity.created(uri)
                .body(TimeResponseDto.from(time));
    }

    @GetMapping
    public ResponseEntity<List<TimeResponseDto>> findAll() {
        List<Time> times = timeService.findAll();
        List<TimeResponseDto> responses = times.stream()
                .map(TimeResponseDto::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeResponseDto> findById(@PathVariable Long id) {
        Time timeById = timeService.findById(id);
        return ResponseEntity.ok(TimeResponseDto.from(timeById));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
