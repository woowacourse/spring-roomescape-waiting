package roomescape.controller.reservation;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final ReservationService reservationService;

    public AdminWaitingController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> getWaitings() {
        return reservationService.getWaitings();
    }
}
