package roomescape.controller.reservation;

import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.exception.ApiException;
import roomescape.exception.ErrorCode;
import roomescape.service.reservation.ReservationService;

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
    public String deleteReservation(
            @PathVariable final Long id,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            reservationService.deleteById(id, LocalDateTime.now());
        } catch (ApiException exception) {
            redirectAttributes.addAttribute("errorCode", exception.getCode());
            return "redirect:/pages/admin/reservations";
        } catch (Exception exception) {
            redirectAttributes.addAttribute("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
            return "redirect:/pages/admin/reservations";
        }

        return "redirect:/pages/admin/reservations";
    }
}
