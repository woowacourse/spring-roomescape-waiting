package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.ReservationTimesResponse;
import roomescape.domain.ReservationTime;
import roomescape.service.ReservationTimeService;

import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<ReservationTimesResponse> findTimes() {
        List<ReservationTime> times = reservationTimeService.findTimes();
        ReservationTimesResponse response = ReservationTimesResponse.from(times);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
