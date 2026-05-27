package roomescape.waiting.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dto.ReservationWaitingRequest;
import roomescape.waiting.dto.ReservationWaitingResponse;
import roomescape.waiting.service.ReservationWaitingService;

import java.net.URI;

@RestController
@RequestMapping("/reservation-waitings")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> create(@Valid @RequestBody ReservationWaitingRequest request) {
        ReservationWaiting reservationWaiting = reservationWaitingService.add(
                request.name(),
                request.themeId(),
                request.date(),
                request.timeId()
        );

        // 상의 필요
        URI location = URI.create("/reservation-waitings/" + reservationWaiting.getId());

        return ResponseEntity.created(location).body(ReservationWaitingResponse.from(reservationWaiting));
    }
}
