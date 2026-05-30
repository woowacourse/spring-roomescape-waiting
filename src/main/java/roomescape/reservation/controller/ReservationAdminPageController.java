package roomescape.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@Controller
@RequestMapping("/pages/admin/reservations")
public class ReservationAdminPageController {

    private final ReservationService reservationService;

    public ReservationAdminPageController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public String getReservationAdminPage(
            @RequestParam(required = false) final String errorCode,
            final Model model
    ) {
        model.addAttribute("reservations", reservationService.getAll().stream()
                .map(ReservationResponse::from)
                .toList());
        model.addAttribute("errorCode", errorCode);
        return "reservation/admin-list";
    }

    @PostMapping("/{id}/delete")
    public String deleteReservation(@PathVariable final Long id) {
        reservationService.deleteById(id);
        return "redirect:/pages/admin/reservations";
    }
}
