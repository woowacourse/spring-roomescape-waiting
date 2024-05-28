package roomescape.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import roomescape.dto.AvailableReservationTimeResponse;
import roomescape.dto.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;

@RequestMapping("/times")
@RestController
public class TimeController {

    ReservationTimeService reservationTimeService;

    public TimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getTimes() {
        List<ReservationTimeResponse> responses = reservationTimeService.getAllReservationTimes();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> getReservationTimeBookedStatus(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        List<AvailableReservationTimeResponse> responses = reservationTimeService
                .getReservationTimeBookedStatus(date, themeId);
        return ResponseEntity.ok().body(responses);
    }
}
