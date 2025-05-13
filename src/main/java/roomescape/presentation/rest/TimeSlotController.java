package roomescape.presentation.rest;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.application.TimeSlotService;
import roomescape.presentation.request.CreateTimeSlotRequest;
import roomescape.presentation.response.AvailableTimeSlotResponse;
import roomescape.presentation.response.TimeSlotResponse;

@Controller
public class TimeSlotController {

    private final TimeSlotService service;

    public TimeSlotController(final TimeSlotService service) {
        this.service = service;
    }

    @PostMapping("/times")
    public ResponseEntity<TimeSlotResponse> register(@RequestBody @Valid final CreateTimeSlotRequest request) {
        var timeSlot = service.register(request.startAt());
        var response = TimeSlotResponse.from(timeSlot);
        return ResponseEntity.created(URI.create("/times/" + timeSlot.id())).body(response);
    }

    @GetMapping("/times")
    public ResponseEntity<List<TimeSlotResponse>> getAllTimeSlots() {
        var timeSlots = service.findAllTimeSlots();
        var response = TimeSlotResponse.from(timeSlots);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/availableTimes", params = {"date", "themeId"})
    public ResponseEntity<List<AvailableTimeSlotResponse>> getAvailableTimes(
        @RequestParam("date") final LocalDate date,
        @RequestParam("themeId") final Long themeId
    ) {
        var availableTimeSlots = service.findAvailableTimeSlots(date, themeId);
        var response = AvailableTimeSlotResponse.from(availableTimeSlots);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        boolean isRemoved = service.removeById(id);
        if (isRemoved) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
