package roomescape.controller.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.exception.ErrorCode;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;

@Component
public class ReservationPageModelAssembler {

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationPageModelAssembler(
            final ReservationService reservationService,
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService
    ) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    public ThemeResponse resolveSelectedTheme(final Long themeId) {
        if (themeId == null) {
            return null;
        }

        return ThemeResponse.from(themeService.getById(themeId));
    }

    public void populateReservationPage(
            final Model model,
            final Long selectedThemeId,
            final ThemeResponse selectedTheme,
            final LocalDate selectedDate,
            final String reservationName,
            final int period,
            final int limit,
            final String errorCode
    ) {
        model.addAttribute("themes", themeService.getAll().stream()
                .map(ThemeResponse::from)
                .toList());
        model.addAttribute("popularThemes", themeService.getPopularThemes(period, limit).stream()
                .map(ThemeResponse::from)
                .toList());
        model.addAttribute("selectedThemeId", selectedThemeId);
        model.addAttribute("selectedTheme", selectedTheme);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("reservationName", reservationName);
        model.addAttribute("period", period);
        model.addAttribute("limit", limit);
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("reservationTimes", reservationTimeService.getAll().stream()
                .map(ReservationTimeResponse::from)
                .toList());
        model.addAttribute("availableTimes", getAvailableTimes(selectedThemeId, selectedTheme, selectedDate));
        model.addAttribute("myReservations", getMyReservations(reservationName, errorCode));
    }

    private List<ReservationTimeResponse> getAvailableTimes(
            final Long selectedThemeId,
            final ThemeResponse selectedTheme,
            final LocalDate selectedDate
    ) {
        if (selectedTheme == null || selectedDate == null) {
            return List.of();
        }

        return reservationTimeService.findAvailableTimes(selectedDate, selectedThemeId).stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    private List<ReservationResponse> getMyReservations(final String reservationName, final String errorCode) {
        if (reservationName == null || reservationName.isBlank()) {
            return List.of();
        }

        if (ErrorCode.INVALID_INPUT.getCode().equals(errorCode)) {
            return List.of();
        }

        return reservationService.getAllByName(reservationName).stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
