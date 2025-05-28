package roomescape.reservationTime.controller.api;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationTime.controller.dto.request.TimeRequest;
import roomescape.reservationTime.controller.dto.response.TimeResponse;
import roomescape.reservationTime.service.TimeService;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeService timeService;

    private TimeController(final TimeService timeService) {
        this.timeService = timeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeResponse createReservationTime(@RequestBody @Valid TimeRequest request) {
        return TimeResponse.from(timeService.addReservationTime(request));
    }

    @GetMapping
    public List<TimeResponse> readReservationTimes() {
        return timeService.findAllReservationTimes().stream()
                .map(TimeResponse::from)
                .toList();
    }

    @GetMapping("/{date}/{themeId}")
    public List<TimeResponse> readReservationTimesWithBooked(
            final @PathVariable("date") LocalDate date,
            final @PathVariable("themeId") long themeId
    ) {
        return timeService.findAllTimesWithBooked(date, themeId).stream()
                .map(TimeResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservationTime(final @PathVariable("id") long id) {
        timeService.removeReservationTime(id);
    }
}
