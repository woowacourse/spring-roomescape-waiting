package roomescape.controller.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.exception.ApiException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.service.reservation.ReservationService;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.service.reservationwaiting.ReservationWaitingService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pages/user/reservations")
public class ReservationPageController {

    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;
    private final ReservationPageModelAssembler reservationPageModelAssembler;

    public ReservationPageController(
            final ReservationService reservationService,
            final ReservationWaitingService reservationWaitingService,
            final ReservationPageModelAssembler reservationPageModelAssembler
    ) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
        this.reservationPageModelAssembler = reservationPageModelAssembler;
    }

    @GetMapping
    public String getReservationPage(
            @RequestParam(required = false) final Long themeId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) final LocalDate date,
            @RequestParam(required = false) final String reservationName,
            @RequestParam(defaultValue = "7") final int period,
            @RequestParam(defaultValue = "10") final int limit,
            @RequestParam(required = false) final String errorCode,
            final Model model
    ) {
        LocalDateTime requestedAt = LocalDateTime.now();
        ThemeResponse selectedTheme = null;
        String resolvedErrorCode = errorCode;

        try {
            selectedTheme = reservationPageModelAssembler.resolveSelectedTheme(themeId);
        } catch (ApiException exception) {
            resolvedErrorCode = resolveErrorCode(resolvedErrorCode, exception.getCode());
        }

        reservationPageModelAssembler.populateReservationPage(
                model,
                themeId,
                selectedTheme,
                date,
                reservationName,
                period,
                limit,
                resolvedErrorCode,
                requestedAt
        );

        return "reservation/list";
    }

    @PostMapping
    public String createReservation(
            @RequestParam(required = false) final String name,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) final LocalDate date,
            @RequestParam(required = false) final Long themeId,
            @RequestParam(required = false) final Long timeId,
            final RedirectAttributes redirectAttributes
    ) {
        LocalDateTime requestedAt = LocalDateTime.now();
        try {
            reservationService.save(
                    name,
                    date,
                    requireId(themeId, "themeId는 필수입니다."),
                    requireId(timeId, "timeId는 필수입니다."),
                    requestedAt
            );
            addReservationNameAttribute(redirectAttributes, name);
        } catch (ApiException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    themeId,
                    date,
                    name,
                    exception.getCode()
            );
        } catch (IllegalArgumentException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    themeId,
                    date,
                    name,
                    ErrorCode.INVALID_INPUT.getCode()
            );
        } catch (Exception exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    themeId,
                    date,
                    name,
                    ErrorCode.INTERNAL_SERVER_ERROR.getCode()
            );
        }

        return "redirect:/pages/user/reservations";
    }

    @PostMapping("/waitings")
    public String createReservationWaiting(
            @RequestParam(required = false) final String name,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) final LocalDate date,
            @RequestParam(required = false) final Long themeId,
            @RequestParam(required = false) final Long timeId,
            final RedirectAttributes redirectAttributes
    ) {
        LocalDateTime requestedAt = LocalDateTime.now();
        try {
            reservationWaitingService.save(
                    name,
                    date,
                    requireId(themeId, "themeId는 필수입니다."),
                    requireId(timeId, "timeId는 필수입니다."),
                    requestedAt
            );
            addReservationNameAttribute(redirectAttributes, name);
            addThemeIdAttribute(redirectAttributes, themeId);
            addDateAttribute(redirectAttributes, date);
        } catch (ApiException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    themeId,
                    date,
                    name,
                    exception.getCode()
            );
        } catch (IllegalArgumentException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    themeId,
                    date,
                    name,
                    ErrorCode.INVALID_INPUT.getCode()
            );
        } catch (Exception exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    themeId,
                    date,
                    name,
                    ErrorCode.INTERNAL_SERVER_ERROR.getCode()
            );
        }

        return "redirect:/pages/user/reservations";
    }

    @PostMapping("/waitings/{id}/delete")
    public String deleteReservationWaiting(
            @PathVariable final Long id,
            @RequestParam(required = false) final String reservationName,
            @RequestParam(required = false) final Long themeId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) final LocalDate date,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            reservationWaitingService.deleteByIdAndName(id, reservationName);
            addReservationNameAttribute(redirectAttributes, reservationName);
            addThemeIdAttribute(redirectAttributes, themeId);
            addDateAttribute(redirectAttributes, date);
        } catch (ApiException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    themeId,
                    date,
                    reservationName,
                    exception.getCode()
            );
        } catch (IllegalArgumentException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    themeId,
                    date,
                    reservationName,
                    ErrorCode.INVALID_INPUT.getCode()
            );
        } catch (Exception exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    themeId,
                    date,
                    reservationName,
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
        LocalDateTime requestedAt = LocalDateTime.now();
        try {
            reservationService.deleteByIdAndName(id, reservationName, requestedAt);
        } catch (ApiException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    null,
                    null,
                    reservationName,
                    exception.getCode()
            );
        } catch (IllegalArgumentException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    null,
                    null,
                    reservationName,
                    ErrorCode.INVALID_INPUT.getCode()
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
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) final LocalDate date,
            @RequestParam(required = false) final Long timeId,
            final RedirectAttributes redirectAttributes
    ) {
        LocalDateTime requestedAt = LocalDateTime.now();
        try {
            reservationService.updateByIdAndName(
                    id,
                    reservationName,
                    date,
                    requireId(timeId, "timeId는 필수입니다."),
                    requestedAt
            );
        } catch (ApiException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    null,
                    null,
                    reservationName,
                    exception.getCode()
            );
        } catch (IllegalArgumentException exception) {
            return redirectReservationPageWithError(
                    redirectAttributes,
                    null,
                    null,
                    reservationName,
                    ErrorCode.INVALID_INPUT.getCode()
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

    private long requireId(final Long id, final String message) {
        if (id == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, message);
        }

        return id;
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
