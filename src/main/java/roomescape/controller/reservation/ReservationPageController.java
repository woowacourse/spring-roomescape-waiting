package roomescape.controller.reservation;

import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.exception.ApiException;
import roomescape.exception.ErrorCode;
import roomescape.service.reservation.ReservationService;
import roomescape.controller.theme.dto.ThemeResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pages/user/reservations")
public class ReservationPageController {

    private final ReservationService reservationService;
    private final ReservationPageRequestParser reservationPageRequestParser;
    private final ReservationPageModelAssembler reservationPageModelAssembler;

    public ReservationPageController(
            final ReservationService reservationService,
            final ReservationPageRequestParser reservationPageRequestParser,
            final ReservationPageModelAssembler reservationPageModelAssembler
    ) {
        this.reservationService = reservationService;
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
        Long selectedThemeId = null;
        LocalDate selectedDate = null;
        ThemeResponse selectedTheme = null;
        String resolvedErrorCode = errorCode;

        try {
            selectedThemeId = reservationPageRequestParser.parseLongValue(themeId);
            selectedDate = reservationPageRequestParser.parseDate(date);
            selectedTheme = reservationPageModelAssembler.resolveSelectedTheme(selectedThemeId);
        } catch (ApiException exception) {
            resolvedErrorCode = resolveErrorCode(resolvedErrorCode, exception.getCode());
        }

        reservationPageModelAssembler.populateReservationPage(
                model,
                selectedThemeId,
                selectedTheme,
                selectedDate,
                reservationName,
                period,
                limit,
                resolvedErrorCode
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
        Long parsedThemeId = null;
        Long parsedTimeId = null;
        LocalDate parsedDate = null;

        try {
            parsedThemeId = reservationPageRequestParser.parseLongValue(themeId);
            parsedTimeId = reservationPageRequestParser.parseLongValue(timeId);
            parsedDate = reservationPageRequestParser.parseDate(date);
            reservationService.save(name, parsedDate, parsedThemeId, parsedTimeId);
            addReservationNameAttribute(redirectAttributes, name);
        } catch (ApiException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    parsedThemeId,
                    parsedDate,
                    name,
                    exception.getCode()
            );
        } catch (Exception exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    parsedThemeId,
                    parsedDate,
                    name,
                    ErrorCode.INTERNAL_SERVER_ERROR.getCode()
            );
        }

        return "redirect:/pages/user/reservations";
    }

    @PostMapping("/{id}/delete")
    public String deleteReservation(
            @PathVariable final Long id,
            @RequestParam(required = false) final String reservationName,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            reservationService.deleteByIdAndName(id, reservationName);
        } catch (ApiException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    null,
                    null,
                    reservationName,
                    exception.getCode()
            );
        } catch (Exception exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    null,
                    null,
                    reservationName,
                    ErrorCode.INTERNAL_SERVER_ERROR.getCode()
            );
        }

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
        Long parsedTimeId = null;
        LocalDate parsedDate = null;

        try {
            parsedTimeId = reservationPageRequestParser.parseLongValue(timeId);
            parsedDate = reservationPageRequestParser.parseDate(date);
            reservationService.updateByIdAndName(id, reservationName, parsedDate, parsedTimeId);
        } catch (ApiException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    null,
                    null,
                    reservationName,
                    exception.getCode()
            );
        } catch (Exception exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    null,
                    null,
                    reservationName,
                    ErrorCode.INTERNAL_SERVER_ERROR.getCode()
            );
        }

        addReservationNameAttribute(redirectAttributes, reservationName);
        return "redirect:/pages/user/reservations";
    }

    private String redirectReservationPageWithError(
            final RedirectAttributes redirectAttributes,
            final Long themeId,
            final LocalDate date,
            final String reservationName,
            final String errorCode
    ) {
        addThemeIdAttribute(redirectAttributes, themeId);
        addDateAttribute(redirectAttributes, date);
        addReservationNameAttribute(redirectAttributes, reservationName);
        redirectAttributes.addAttribute("errorCode", errorCode);
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

    private void addReservationNameAttribute(final RedirectAttributes redirectAttributes, final String reservationName) {
        if (reservationName == null || reservationName.isBlank()) {
            return;
        }

        redirectAttributes.addAttribute("reservationName", reservationName);
    }

    private String resolveErrorCode(final String currentErrorCode, final String fallbackErrorCode) {
        if (currentErrorCode != null) {
            return currentErrorCode;
        }

        return fallbackErrorCode;
    }
}
