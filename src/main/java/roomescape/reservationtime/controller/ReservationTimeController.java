package roomescape.reservationtime.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationtime.controller.dto.AvailableTimeResponse;
import roomescape.reservationtime.controller.dto.AvailableTimesResponse;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.reservationtime.controller.dto.ReservationTimesResponse;
import roomescape.reservationtime.service.ReservationTimeAvailability;
import roomescape.reservationtime.service.ReservationTimeService;

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
    public ResponseEntity<ReservationTimesResponse> list() {
        return ResponseEntity.ok(ReservationTimesResponse.from(
                reservationTimeService.findAll()
                        .stream()
                        .map(ReservationTimeResponse::from)
                        .toList()));
    }

    @GetMapping("/availability")
    public ResponseEntity<AvailableTimesResponse> getAvailableTimes(@RequestParam("date") LocalDate date,
                                                                    @Positive(message = "테마 id는 1 이상의 숫자여야 합니다.")
                                                                    @RequestParam("themeId") Long themeId) {
        List<ReservationTimeAvailability> timeAvailabilities =
                reservationTimeService.findAvailableTimes(date, themeId);

        return ResponseEntity.ok(AvailableTimesResponse.from(timeAvailabilities.stream()
                .map(AvailableTimeResponse::from)
                .toList()));
    }

}
