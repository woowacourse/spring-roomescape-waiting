package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.TimeRequest;
import roomescape.controller.dto.TimeResponse;
import roomescape.controller.dto.TimeResponses;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.service.TimeSlotService;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeSlotService reservationTimeSlotService;

    public TimeController(TimeSlotService reservationTimeSlotService) {
        this.reservationTimeSlotService = reservationTimeSlotService;
    }

    @GetMapping
    public ResponseEntity<TimeResponses> getTimes() {
        return ResponseEntity.ok(TimeResponses.from(reservationTimeSlotService.findAllTimes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeResponse> getTime(@PathVariable long id) {
        TimeSlot timeSlot = reservationTimeSlotService.getTimeSlotById(id);
        return ResponseEntity.ok(TimeResponse.from(timeSlot));
    }

    @GetMapping(params = {"themeId", "date"})
    public ResponseEntity<TimeResponses> getAvailableTimes(
            @RequestParam("themeId") long themeId,
            @RequestParam("date") LocalDate date
    ) {

        return ResponseEntity.ok(
                TimeResponses.fromAvailable(reservationTimeSlotService.getAvailableTimes(themeId, date)));
    }

    @PostMapping
    public ResponseEntity<TimeResponse> createTime(@RequestBody @Valid TimeRequest request) {
        TimeSlot timeSlot = reservationTimeSlotService.saveTime(request.startAt());
        return ResponseEntity.created(URI.create("/times/" + timeSlot.getId()))
                .body(TimeResponse.from(timeSlot));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable long id) {
        reservationTimeSlotService.removeTime(id);
        return ResponseEntity.noContent().build();
    }
}
