package roomescape.admin.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    private final ReservationRepository reservationRepository;

    public AdminPageController(ReservationRepository ReservationRepository) {
        this.reservationRepository = ReservationRepository;
    }

    @GetMapping
    public String getAdminHome() {
        return "/admin/index";
    }

    @GetMapping("/time")
    public String getAdminTimePage() {
        return "/admin/time";
    }

    @GetMapping("/theme")
    public String getAdminThemePage() {
        return "/admin/theme";
    }

    @GetMapping("/reservation")
    public String getReservationPage(Model model) {
        List<ReservationResponse> reservationResponses = new ArrayList<>();
        for (Reservation reservation : reservationRepository.findAll()) {
            reservationResponses.add(new ReservationResponse(reservation));
        }
        model.addAttribute("reservationResponses", reservationResponses);
        return "/admin/reservation-new";
    }
}
