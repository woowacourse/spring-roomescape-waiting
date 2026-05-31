package roomescape.presentation;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.reservation.application.ReservationService;
import roomescape.theme.application.ThemeService;
import roomescape.time.application.ReservationTimeService;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final ThemeService themeService;
    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;

    @GetMapping({"/", "/reservation"})
    public String reservationPage(Model model) {
        model.addAttribute("themes", themeService.getThemes());
        model.addAttribute("popularThemes", themeService.getWeeksTopThemes());
        model.addAttribute("today", LocalDate.now());
        return "reservation";
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("themes", themeService.getThemes());
        model.addAttribute("times", reservationTimeService.getReservationTimes());
        model.addAttribute("reservations", reservationService.getReservations());
        return "admin";
    }
}
