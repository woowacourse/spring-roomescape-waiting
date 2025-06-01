package roomescape.timeslot.presentation.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.timeslot.application.dto.TimeSlotAvailabilityInfo;
import roomescape.timeslot.application.dto.TimeSlotCreateCommand;
import roomescape.timeslot.application.dto.TimeSlotInfo;
import roomescape.timeslot.application.service.TimeSlotService;
import roomescape.timeslot.presentation.dto.TimeSlotAvailabilityResponse;
import roomescape.timeslot.presentation.dto.TimeSlotCreateRequest;
import roomescape.timeslot.presentation.dto.TimeSlotResponse;

@RestController
@RequestMapping("/times")
public class TimeSlotApiController {

    private final TimeSlotService timeSlotService;

    public TimeSlotApiController(final TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping
    public ResponseEntity<TimeSlotResponse> create(
            @RequestBody @Valid final TimeSlotCreateRequest request) {
        final TimeSlotCreateCommand command = request.convertToCreateCommand();
        final TimeSlotInfo timeInfo = timeSlotService.createTimeSlot(command);
        final URI uri = URI.create("/times/" + timeInfo.id());
        final TimeSlotResponse response = new TimeSlotResponse(timeInfo);
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TimeSlotResponse>> findAll() {
        final List<TimeSlotInfo> timeInfos = timeSlotService.findTimeSlots();
        final List<TimeSlotResponse> responses = timeInfos.stream()
                .map(TimeSlotResponse::new)
                .toList();
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        timeSlotService.deleteTimeSlotById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availability")
    public ResponseEntity<List<TimeSlotAvailabilityResponse>> findAvailableTimes(
            @RequestParam("date") final LocalDate date,
            @RequestParam("themeId") final long themeId
    ) {
        final List<TimeSlotAvailabilityInfo> timeInfos = timeSlotService.findAvailableTimeSlots(date, themeId);
        final List<TimeSlotAvailabilityResponse> responses = timeInfos.stream()
                .map(TimeSlotAvailabilityResponse::new)
                .toList();
        return ResponseEntity.ok().body(responses);
    }
}
