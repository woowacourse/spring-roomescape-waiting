package roomescape.controller.reservationmine;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.service.reservationmine.ReservationMineService;

@Controller
@RequestMapping("/pages/user/reservations-mine")
public class ReservationMinePageController {

    private final ReservationMineService reservationMineService;

    public ReservationMinePageController(final ReservationMineService reservationMineService) {
        this.reservationMineService = reservationMineService;
    }

    @GetMapping
    public String getReservationsMine(
            @RequestParam(required = false) final String name,
            final Model model
    ) {
        List<ReservationResponse> reservations = reservationMineService.getAllByName(name);
        model.addAttribute("reservationName", name);
        model.addAttribute("reservations", reservations);
        return "reservation/mine-list";
    }
}
