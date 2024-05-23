package roomescape.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationJpaRepository;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    private final ReservationJpaRepository reservationJpaRepository;

    public AdminPageController(ReservationJpaRepository ReservationJpaRepository) {
        this.reservationJpaRepository = ReservationJpaRepository;
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
        List<Reservation> reservations = reservationJpaRepository.findAll();
        List<ReservationResponse> reservationResponses = reservations
                .stream()
                .map(ReservationResponse::new)
                .toList();

        model.addAttribute("reservationResponses", reservationResponses);
        return "/admin/reservation-new";
    }

    @GetMapping("waiting")
    public String getAdminWaitingPage() {
        return "/admin/waiting";
    }
}
