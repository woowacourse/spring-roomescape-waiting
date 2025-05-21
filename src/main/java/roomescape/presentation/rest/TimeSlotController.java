package roomescape.presentation.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.TimeSlotService;
import roomescape.presentation.request.CreateTimeSlotRequest;
import roomescape.presentation.response.AvailableTimeSlotResponse;
import roomescape.presentation.response.TimeSlotResponse;

@RestController
public class TimeSlotController {

    private final TimeSlotService service;

    public TimeSlotController(final TimeSlotService service) {
        this.service = service;
    }

    @PostMapping("/times")
    @ResponseStatus(CREATED)
    public TimeSlotResponse register(@RequestBody @Valid final CreateTimeSlotRequest request) {
        var timeSlot = service.register(request.startAt());
        return TimeSlotResponse.from(timeSlot);
    }

    @GetMapping("/times")
    public List<TimeSlotResponse> getAllTimeSlots() {
        var timeSlots = service.findAllTimeSlots();
        return TimeSlotResponse.from(timeSlots);
    }

    @GetMapping(value = "/availableTimes", params = {"date", "themeId"})
    public List<AvailableTimeSlotResponse> getAvailableTimes(
            @RequestParam("date") final LocalDate date,
            @RequestParam("themeId") final Long themeId
    ) {
        var availableTimeSlots = service.findAvailableTimeSlots(date, themeId);
        return AvailableTimeSlotResponse.from(availableTimeSlots);
    }

    @DeleteMapping("/times/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable("id") final long id) {
        service.removeById(id);
    }
}
