package roomescape.time.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import roomescape.time.dto.ReservationTimeCreateRequest;
import roomescape.time.dto.ReservationTimeResponse;
import roomescape.time.service.ReservationTimeService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public List<ReservationTimeResponse> readTimes() {
        return reservationTimeService.readReservationTimes();
    }

    @GetMapping(params = {"date", "themeId"})
    public List<ReservationTimeResponse> readTimes(
            @RequestParam(value = "date") LocalDate date,
            @RequestParam(value = "themeId") Long themeId
    ) {
        return reservationTimeService.readReservationTimes(date, themeId);
    }

    @GetMapping("/{id}")
    public ReservationTimeResponse readTime(@PathVariable Long id) {
        return reservationTimeService.readReservationTime(id);

    }

    @PostMapping
    public ReservationTimeResponse createTime(@Valid @RequestBody ReservationTimeCreateRequest request) {
        return reservationTimeService.createTime(request);
    }

    @DeleteMapping("/{id}")
    public void deleteTime(@PathVariable Long id) {
        reservationTimeService.deleteTime(id);
    }
}
