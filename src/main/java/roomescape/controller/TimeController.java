package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.TimePatchRequest;
import roomescape.controller.dto.TimePutRequest;
import roomescape.controller.dto.TimeRequest;
import roomescape.controller.dto.TimeResponse;
import roomescape.controller.dto.TimeResponses;
import roomescape.domain.TimeSlot;
import roomescape.service.TimeSlotService;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeSlotService reservationTimeSlotService;

    public TimeController(TimeSlotService reservationTimeSlotService) {
        this.reservationTimeSlotService = reservationTimeSlotService;
    }

    @GetMapping
    public ResponseEntity<TimeResponses> times() {
        return ResponseEntity.ok(TimeResponses.from(reservationTimeSlotService.allTimes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeResponse> getTime(@PathVariable long id) {
        TimeSlot timeSlot = reservationTimeSlotService.findTimeSlotById(id);
        return ResponseEntity.ok(TimeResponse.from(timeSlot));
    }

    @GetMapping(params = {"themeId", "date"})
    public ResponseEntity<TimeResponses> getAvailableTimes(
            @RequestParam("themeId") long themeId,
            @RequestParam("date") LocalDate date
    ) {

        return ResponseEntity.ok(
                TimeResponses.fromAvailable(reservationTimeSlotService.findAvailableTimes(themeId, date)));
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

    @PutMapping("/{id}")
    public ResponseEntity<TimeResponse> updateTime(
            @PathVariable long id,
            @RequestBody @Valid TimePutRequest request
    ) {
        reservationTimeSlotService.putTime(id, request.startAt());
        return ResponseEntity.ok(TimeResponse.from(reservationTimeSlotService.findTimeSlotById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TimeResponse> patchTime(
            @PathVariable long id,
            @RequestBody TimePatchRequest request
    ) {
        reservationTimeSlotService.patchTime(id, request.startAt());
        return ResponseEntity.ok(TimeResponse.from(reservationTimeSlotService.findTimeSlotById(id)));
    }
}
