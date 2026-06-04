package roomescape.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.ReservationTime;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.service.ReservationTimeQueryService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
@RequiredArgsConstructor
public class TimeController {

    private final ReservationTimeQueryService reservationTimeQueryService;

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getAvailableTimes(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") long themeId) {
        List<ReservationTime> availableReservationTimes = reservationTimeQueryService.findAvailableReservationTimes(date, themeId);
        List<ReservationTimeResponse> reservationTimeResponses = availableReservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(reservationTimeResponses);
    }
}
