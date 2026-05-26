package roomescape.domain.time.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.time.dto.request.TimeCreateRequestDto;
import roomescape.domain.time.dto.response.TimeResponseDto;
import roomescape.domain.time.service.TimeService;

@RestController
@RequestMapping("/api/admin/times")
@Validated
public class AdminTimeController {

    private final TimeService timeService;

    public AdminTimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping
    public ResponseEntity<List<TimeResponseDto>> getTimes() {
        return ResponseEntity.ok(timeService.getTimes());
    }

    @PostMapping
    public ResponseEntity<TimeResponseDto> saveTime(@Valid @RequestBody TimeCreateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeService.saveTime(requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id) {
        timeService.deleteTimeById(id);
        return ResponseEntity.noContent().build();
    }
}
