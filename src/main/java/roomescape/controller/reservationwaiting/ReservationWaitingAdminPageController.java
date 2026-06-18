package roomescape.controller.reservationwaiting;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import roomescape.controller.reservationwaiting.dto.ReservationWaitingResponse;
import roomescape.exception.ApiException;
import roomescape.exception.ErrorCode;
import roomescape.service.reservationwaiting.ReservationWaitingService;

@Controller
@RequestMapping("/pages/admin/waitings")
public class ReservationWaitingAdminPageController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingAdminPageController(final ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping
    public String getReservationWaitingAdminPage(
            @RequestParam(required = false) final String errorCode,
            final Model model
    ) {
        model.addAttribute("waitings", reservationWaitingService.getAll().stream()
                .map(ReservationWaitingResponse::from)
                .toList());
        model.addAttribute("errorCode", errorCode);
        return "reservationwaiting/admin-list";
    }

    @PostMapping("/{id}/delete")
    public String deleteReservationWaiting(
            @PathVariable final Long id,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            reservationWaitingService.deleteById(id);
        } catch (ApiException exception) {
            redirectAttributes.addAttribute("errorCode", exception.getCode());
            return "redirect:/pages/admin/waitings";
        } catch (Exception exception) {
            redirectAttributes.addAttribute("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
            return "redirect:/pages/admin/waitings";
        }

        return "redirect:/pages/admin/waitings";
    }
}
