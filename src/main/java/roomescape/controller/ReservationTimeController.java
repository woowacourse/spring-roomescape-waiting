package roomescape.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.AvailableReservationTimeResponse;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping(params = {"themeId", "baseDate"})
    public ResponseEntity<List<AvailableReservationTimeResponse>> readReservationTimes(@RequestParam(value = "themeId") long themeId,
                                                                                       @RequestParam(value = "baseDate") LocalDate baseDate) {
        List<AvailableReservationTimeResponse> responses = reservationTimeService.getReservationTimes(
                themeId,
                baseDate,
                LocalDate.now());
        return ResponseEntity.ok(responses);
    }
}
