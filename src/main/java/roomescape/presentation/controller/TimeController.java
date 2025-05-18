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
import roomescape.application.dto.TimeCreateServiceRequest;
import roomescape.application.dto.TimeServiceResponse;
import roomescape.domain.repository.dto.TimeDataWithBookingInfo;
import roomescape.presentation.controller.dto.TimeDataWithBookingInfoResponse;
import roomescape.presentation.controller.dto.TimeResponse;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeService service;

    public TimeController(TimeService service) {
        this.service = service;
    }

    @GetMapping
    public List<TimeResponse> getAllTimes() {
        return TimeResponse.from(service.getAllTimes());
    }

    @PostMapping
    public ResponseEntity<TimeResponse> addTime(@Valid @RequestBody TimeCreateServiceRequest request) {
        TimeServiceResponse timeServiceResponse = service.registerNewTime(request);
        TimeResponse timeResponse = TimeResponse.from(timeServiceResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(timeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable(name = "id") Long id) {
        service.deleteTime(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/booking-status")
    public ResponseEntity<List<TimeDataWithBookingInfoResponse>> getTimesWithBookingInfo(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        List<TimeDataWithBookingInfo> timesWithBookingInfo = service.getTimesWithBookingInfo(date, themeId);
        List<TimeDataWithBookingInfoResponse> response = TimeDataWithBookingInfoResponse.from(timesWithBookingInfo);
        return ResponseEntity.ok().body(response);
    }
}
