package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.TimePatchRequest;
import roomescape.controller.dto.TimeRequest;
import roomescape.controller.dto.TimeResponse;
import roomescape.domain.TimeSlot;
import roomescape.service.TimeSlotService;
import roomescape.service.dto.AvailableTimeSlot;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeSlotService reservationTimeSlotService;

    public TimeController(TimeSlotService reservationTimeSlotService) {
        this.reservationTimeSlotService = reservationTimeSlotService;
    }

    @GetMapping
    public ResponseEntity<List<TimeResponse>> times() {
        return ResponseEntity.ok(convertToTimeResponses(reservationTimeSlotService.allTimes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeResponse> getTime(@PathVariable long id) {
        TimeSlot timeSlot = reservationTimeSlotService.findTimeSlotById(id);
        return ResponseEntity.ok(TimeResponse.from(timeSlot));
    }

    @GetMapping(params = {"themeId", "date"})
    public ResponseEntity<List<TimeResponse>> getAvailableTimes(
            @RequestParam("themeId")
            long themeId,
            @RequestParam("date")
            LocalDate date
    ) {
        List<AvailableTimeSlot> availableSlots = reservationTimeSlotService.findAvailableTimes(themeId, date);
        return ResponseEntity.ok(convertToAvailableResponses(availableSlots));
    }

    @PostMapping
    public ResponseEntity<TimeResponse> createTime(@RequestBody @Valid TimeRequest request) {
        TimeSlot timeSlot = reservationTimeSlotService.saveTime(request);
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
            @PathVariable
            long id,
            @RequestBody @Valid
            TimeRequest request
    ) {
        return ResponseEntity.ok(TimeResponse.from(reservationTimeSlotService.putTime(id, request)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TimeResponse> patchTime(
            @PathVariable
            long id,
            @RequestBody
            TimePatchRequest request
    ) {
        return ResponseEntity.ok(TimeResponse.from(reservationTimeSlotService.patchTime(id, request)));
    }

    private List<TimeResponse> convertToTimeResponses(List<TimeSlot> reservationTimeSlots) {
        return reservationTimeSlots.stream()
                .map(TimeResponse::from)
                .toList();
    }

    private List<TimeResponse> convertToAvailableResponses(List<AvailableTimeSlot> availableSlots) {
        return availableSlots.stream()
                .map(TimeResponse::from)
                .toList();
    }
}
