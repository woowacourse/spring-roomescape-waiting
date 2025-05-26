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
import roomescape.domain.timeslot.AvailableTimeSlot;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.presentation.request.CreateTimeSlotRequest;
import roomescape.presentation.response.AvailableTimeSlotResponse;
import roomescape.presentation.response.TimeSlotResponse;

@RestController
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(final TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping("/admin/times")
    @ResponseStatus(CREATED)
    public TimeSlotResponse createTimeSlot(
            @RequestBody @Valid final CreateTimeSlotRequest request
    ) {
        TimeSlot timeSlot = timeSlotService.saveTimeSlot(request.startAt());

        return TimeSlotResponse.fromTimeSlot(timeSlot);
    }

    @GetMapping("/times")
    public List<TimeSlotResponse> readAllTimeSlots() {
        List<TimeSlot> timeSlots = timeSlotService.findAllTimeSlots();

        return TimeSlotResponse.fromTimeSlots(timeSlots);
    }

    @GetMapping(value = "/availableTimes", params = {"date", "themeId"})
    public List<AvailableTimeSlotResponse> readAvailableTimes(
            @RequestParam("date") final LocalDate date,
            @RequestParam("themeId") final Long themeId
    ) {
        List<AvailableTimeSlot> availableTimeSlots = timeSlotService.findAvailableTimeSlots(date, themeId);

        return AvailableTimeSlotResponse.fromAvailableTimeSlots(availableTimeSlots);
    }

    @DeleteMapping("/admin/times/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteTimeSlotById(
            @PathVariable("id") final long id
    ) {
        timeSlotService.removeById(id);
    }
}
