package roomescape.time.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import roomescape.time.dto.ReservationTimeCreateRequest;
import roomescape.time.dto.TimeBookedResponse;
import roomescape.time.service.ReservationTimeService;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/times")
    public List<TimeBookedResponse> readTimes() {
        return reservationTimeService.readReservationTimes();
    }

    @GetMapping(path = "/times", params = {"date", "themeId"})
    public List<TimeBookedResponse> readTimes(
            @RequestParam(value = "date") LocalDate date,
            @RequestParam(value = "themeId") Long themeId
    ) {
        return reservationTimeService.readReservationTimes(date, themeId);
    }

    @GetMapping("/times/{id}")
    public TimeBookedResponse readTime(@PathVariable Long id) {
        return reservationTimeService.readReservationTime(id);

    }

    @PostMapping("/times")
    public TimeBookedResponse createTime(@Valid @RequestBody ReservationTimeCreateRequest request) {
        return reservationTimeService.createTime(request);
    }

    @DeleteMapping("/times/{id}")
    public void deleteTime(@PathVariable Long id) {
        reservationTimeService.deleteTime(id);
    }
}
