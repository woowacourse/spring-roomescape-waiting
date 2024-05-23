package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.dto.ReservationTimeResponse;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ClientTimeController {
    private final ReservationTimeService reservationTimeService;

    public ClientTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<ResponsesWrapper<ReservationTimeResponse>> findAll() {
        return ResponseEntity.ok(reservationTimeService.findAll());
    }
}
