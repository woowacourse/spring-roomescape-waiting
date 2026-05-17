package roomescape.reservationtime.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservationtime.repository.dto.ReservationTimeAvailability;
import roomescape.reservationtime.controller.dto.AvailableTimeListResponse;
import roomescape.reservationtime.controller.dto.AvailableTimeResponse;
import roomescape.reservationtime.controller.dto.ReservationTimeListResponse;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.reservationtime.service.ReservationTimeService;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/times")
@RequiredArgsConstructor
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    @GetMapping
    public ResponseEntity<ReservationTimeListResponse> getAllReservationTimes() {
        return ResponseEntity.ok(ReservationTimeListResponse.from(
                reservationTimeService.findAllReservationTimes()
                        .stream()
                        .map(ReservationTimeResponse::from)
                        .toList()));
    }

    @GetMapping("/availability")
    public ResponseEntity<AvailableTimeListResponse> getAvailableTimes(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam("themeId") Long themeId) {
        List<ReservationTimeAvailability> timeAvailabilities =
                reservationTimeService.findAvailableTimes(date, themeId);

        return ResponseEntity.ok(AvailableTimeListResponse.from(timeAvailabilities.stream()
                .map(AvailableTimeResponse::from)
                .toList()));
    }

}
