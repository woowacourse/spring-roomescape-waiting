package roomescape.controller.api;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.TimeRequest;
import roomescape.dto.response.TimeResponse;
import roomescape.service.TimeService;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeService timeService;

    private TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping
    public List<TimeResponse> readReservationTimes() {
        return timeService.findAllReservationTimes();
    }

    @GetMapping("/{date}/{themeId}")
    public List<TimeResponse> readReservationTimesWithBooked(@PathVariable LocalDate date,
        @PathVariable Long themeId) {
        return timeService.findAllTimesWithBooked(date, themeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeResponse createReservationTime(@RequestBody TimeRequest request) {
        return timeService.addReservationTime(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservationTime(@PathVariable Long id) {
        timeService.removeReservationTime(id);
    }
}
