package roomescape.reservation.controller.page;

import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import roomescape.application.ReservationApplicationService;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationwaiting.service.ReservationWaitingService;
import roomescape.theme.controller.dto.ThemeResponse;

@Controller
@RequestMapping("/pages/user/reservations")
public class ReservationPageController {

    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;
    private final ReservationApplicationService reservationApplicationService;
    private final ReservationPageRequestParser reservationPageRequestParser;
    private final ReservationPageModelAssembler reservationPageModelAssembler;

    public ReservationPageController(
            final ReservationService reservationService,
            final ReservationWaitingService reservationWaitingService,
            final ReservationApplicationService reservationApplicationService,
            final ReservationPageRequestParser reservationPageRequestParser,
            final ReservationPageModelAssembler reservationPageModelAssembler
    ) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
        this.reservationApplicationService = reservationApplicationService;
        this.reservationPageRequestParser = reservationPageRequestParser;
        this.reservationPageModelAssembler = reservationPageModelAssembler;
    }

    @GetMapping
    public String getReservationPage(
            @RequestParam(required = false) final String themeId,
            @RequestParam(required = false) final String date,
            @RequestParam(required = false) final String reservationName,
            @RequestParam(defaultValue = "7") final int period,
            @RequestParam(defaultValue = "10") final int limit,
            @RequestParam(required = false) final String errorCode,
            final Model model
    ) {
        Long selectedThemeId = reservationPageRequestParser.parseLongValue(themeId);
        LocalDate selectedDate = reservationPageRequestParser.parseDate(date);
        ThemeResponse selectedTheme = reservationPageModelAssembler.resolveSelectedTheme(selectedThemeId);

        reservationPageModelAssembler.populateReservationPage(
                model,
                selectedThemeId,
                selectedTheme,
                selectedDate,
                reservationName,
                period,
                limit,
                errorCode
        );

        return "reservation/list";
    }

    @PostMapping
    public String createReservation(
            @RequestParam(required = false) final String name,
            @RequestParam(required = false) final String date,
            @RequestParam(required = false) final String themeId,
            @RequestParam(required = false) final String timeId,
            final RedirectAttributes redirectAttributes
    ) {
        Long parsedThemeId = reservationPageRequestParser.parseLongValue(themeId);
        Long parsedTimeId = reservationPageRequestParser.parseLongValue(timeId);
        LocalDate parsedDate = reservationPageRequestParser.parseDate(date);
        reservationService.save(name, parsedDate, parsedThemeId, parsedTimeId);

        return "redirect:/pages/user/reservations";
    }

    @PostMapping("/waitings")
    public String createReservationWaiting(
            @RequestParam(required = false) final String name,
            @RequestParam(required = false) final String date,
            @RequestParam(required = false) final String themeId,
            @RequestParam(required = false) final String timeId,
            final RedirectAttributes redirectAttributes
    ) {
        Long parsedThemeId = reservationPageRequestParser.parseLongValue(themeId);
        Long parsedTimeId = reservationPageRequestParser.parseLongValue(timeId);
        LocalDate parsedDate = reservationPageRequestParser.parseDate(date);
        reservationApplicationService.saveWaiting(name, parsedDate, parsedThemeId, parsedTimeId);

        addReservationNameAttribute(redirectAttributes, name);
        addThemeIdAttribute(redirectAttributes, parsedThemeId);
        addDateAttribute(redirectAttributes, parsedDate);
        return "redirect:/pages/user/reservations";
    }

    @PostMapping("/waitings/{id}/delete")
    public String deleteReservationWaiting(
            @PathVariable final Long id,
            @RequestParam(required = false) final String reservationName,
            @RequestParam(required = false) final String themeId,
            @RequestParam(required = false) final String date,
            final RedirectAttributes redirectAttributes
    ) {
        Long parsedThemeId = reservationPageRequestParser.parseLongValue(themeId);
        LocalDate parsedDate = reservationPageRequestParser.parseDate(date);
        reservationWaitingService.deleteByIdAndName(id, reservationName);

        addReservationNameAttribute(redirectAttributes, reservationName);
        addThemeIdAttribute(redirectAttributes, parsedThemeId);
        addDateAttribute(redirectAttributes, parsedDate);
        return "redirect:/pages/user/reservations";
    }

    @PostMapping("/{id}/delete")
    public String deleteReservation(
            @PathVariable final Long id,
            @RequestParam(required = false) final String reservationName,
            final RedirectAttributes redirectAttributes
    ) {
        reservationApplicationService.cancelReservation(id, reservationName);
        addReservationNameAttribute(redirectAttributes, reservationName);
        return "redirect:/pages/user/reservations";
    }

    @PostMapping("/{id}/update")
    public String updateReservation(
            @PathVariable final Long id,
            @RequestParam(required = false) final String reservationName,
            @RequestParam(required = false) final String date,
            @RequestParam(required = false) final String timeId,
            final RedirectAttributes redirectAttributes
    ) {
        Long parsedTimeId = reservationPageRequestParser.parseLongValue(timeId);
        LocalDate parsedDate = reservationPageRequestParser.parseDate(date);
        reservationService.updateByIdAndName(id, reservationName, parsedDate, parsedTimeId);

        addReservationNameAttribute(redirectAttributes, reservationName);
        return "redirect:/pages/user/reservations";
    }

    private void addDateAttribute(final RedirectAttributes redirectAttributes, final LocalDate date) {
        if (date == null) {
            return;
        }
        redirectAttributes.addAttribute("date", date.toString());
    }

    private void addThemeIdAttribute(final RedirectAttributes redirectAttributes, final Long themeId) {
        if (themeId == null) {
            return;
        }
        redirectAttributes.addAttribute("themeId", themeId);
    }

    private void addReservationNameAttribute(
            final RedirectAttributes redirectAttributes,
            final String reservationName
    ) {
        if (reservationName == null || reservationName.isBlank()) {
            return;
        }
        redirectAttributes.addAttribute("reservationName", reservationName);
    }
}
