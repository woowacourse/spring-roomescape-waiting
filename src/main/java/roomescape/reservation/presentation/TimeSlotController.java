package roomescape.reservation.presentation;

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
import roomescape.reservation.dto.request.ReservationTimeRequest;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.dto.response.TimeWithBookedResponse;
import roomescape.reservation.service.TimeSlotService;

@RestController
@RequestMapping(value = "/api/times")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getAllReservationTimes() {
        List<ReservationTimeResponse> all = timeSlotService.findAllTimes();
        return ResponseEntity.ok(all);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createNewReservationTime(
            @Valid @RequestBody ReservationTimeRequest reservationTimeRequest) {
        ReservationTimeResponse reservationTime = timeSlotService.createTime(reservationTimeRequest);
        return ResponseEntity
                .created(URI.create("/api/times/" + reservationTime.id()))
                .body(reservationTime);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long id) {
        timeSlotService.deleteTimeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("theme/{themeId}")
    public ResponseEntity<List<TimeWithBookedResponse>> getTimesWithBooked(
            @PathVariable("themeId") Long themeId, @RequestParam("date") LocalDate date
    ) {
        return ResponseEntity.ok(timeSlotService.findTimesByDateAndThemeIdWithBooked(date, themeId));
    }
}
