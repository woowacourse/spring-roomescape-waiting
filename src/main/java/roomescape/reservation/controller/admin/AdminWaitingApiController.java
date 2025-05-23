package roomescape.reservation.controller.admin;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingApiController {

    private final ReservationService reservationService;

    public AdminWaitingApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<WaitingResponse> findAllWaiting() {
        return reservationService.findAllWaiting();
    }
}
