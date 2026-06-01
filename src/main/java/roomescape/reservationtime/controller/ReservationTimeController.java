package roomescape.reservationtime.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservationtime.controller.dto.ReservationTimeListResponse;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.reservationtime.service.ReservationTimeService;

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
}
