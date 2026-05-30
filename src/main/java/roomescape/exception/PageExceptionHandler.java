package roomescape.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import roomescape.reservation.controller.ReservationAdminPageController;
import roomescape.reservation.controller.ReservationPageController;
import roomescape.reservationtime.controller.ReservationTimeAdminPageController;
import roomescape.theme.controller.ThemeAdminPageController;

@ControllerAdvice(assignableTypes = {
        ReservationPageController.class,
        ReservationAdminPageController.class,
        ReservationTimeAdminPageController.class,
        ThemeAdminPageController.class
})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PageExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public String handleApiException(
            final ApiException exception,
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addAttribute("errorCode", exception.getCode());
        preserveContextParams(request, redirectAttributes);
        return "redirect:" + resolveRedirectUrl(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addAttribute("errorCode", ErrorCode.INVALID_INPUT.getCode());
        preserveContextParams(request, redirectAttributes);
        return "redirect:" + resolveRedirectUrl(request);
    }

    @ExceptionHandler(Exception.class)
    public String handleException(
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addAttribute("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        preserveContextParams(request, redirectAttributes);
        return "redirect:" + resolveRedirectUrl(request);
    }

    private String resolveRedirectUrl(final HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/pages/user/")) {
            return "/pages/user/reservations";
        }
        if (uri.startsWith("/pages/admin/themes")) {
            return "/pages/admin/themes";
        }
        if (uri.startsWith("/pages/admin/reservation-times")) {
            return "/pages/admin/reservation-times";
        }
        return "/pages/admin/reservations";
    }

    private void preserveContextParams(
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes
    ) {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        String reservationName = resolveReservationName(request);
        if (reservationName != null && !reservationName.isBlank()) {
            redirectAttributes.addAttribute("reservationName", reservationName);
        }

        String themeId = request.getParameter("themeId");
        if (isValidLong(themeId)) {
            redirectAttributes.addAttribute("themeId", themeId);
        }

        String date = request.getParameter("date");
        if (isValidDate(date)) {
            redirectAttributes.addAttribute("date", date);
        }
    }

    private String resolveReservationName(final HttpServletRequest request) {
        String reservationName = request.getParameter("reservationName");
        if (reservationName != null && !reservationName.isBlank()) {
            return reservationName;
        }
        return request.getParameter("name");
    }

    private boolean isValidLong(final String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidDate(final String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
