package roomescape.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.reservation.WaitingService;
import roomescape.service.reservation.dto.ReservationResponse;

import java.util.List;

@RestController
@RequestMapping("/waitings")
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public List<ReservationResponse> findAllReservations() {
        return waitingService.findAll();
    }
}
