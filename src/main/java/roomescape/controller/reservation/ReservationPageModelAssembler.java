package roomescape.controller.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import roomescape.controller.history.dto.MyReservationHistoryResponse;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.reservationtime.dto.ReservationTimeSlotResponse;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.exception.ErrorCode;
import roomescape.service.history.MyReservationHistoryService;
import roomescape.service.history.ReservationHistoryStatus;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;

@Component
public class ReservationPageModelAssembler {

    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MyReservationHistoryService myReservationHistoryService;

    public ReservationPageModelAssembler(
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService,
            final MyReservationHistoryService myReservationHistoryService
    ) {
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.myReservationHistoryService = myReservationHistoryService;
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
        List<MyReservationHistoryResponse> myHistories = getMyHistories(reservationName, errorCode);
        model.addAttribute("availableTimes", getAvailableTimes(
                selectedThemeId,
                selectedTheme,
                selectedDate,
                myHistories
        ));
        model.addAttribute("myHistories", myHistories);
    }

    private List<ReservationTimeSlotResponse> getAvailableTimes(
            final Long selectedThemeId,
            final ThemeResponse selectedTheme,
            final LocalDate selectedDate,
            final List<MyReservationHistoryResponse> myHistories
    ) {
        if (selectedTheme == null || selectedDate == null) {
            return List.of();
        }

        return reservationTimeService.findTimeSlots(selectedDate, selectedThemeId).stream()
                .map(slot -> ReservationTimeSlotResponse.from(
                        slot,
                        findWaitingId(myHistories, selectedDate, selectedThemeId, slot.id())
                ))
                .toList();
    }

    private Long findWaitingId(
            final List<MyReservationHistoryResponse> myHistories,
            final LocalDate selectedDate,
            final Long selectedThemeId,
            final Long timeId
    ) {
        return myHistories.stream()
                .filter(history -> history.status() == ReservationHistoryStatus.WAITING)
                .filter(history -> history.date().equals(selectedDate))
                .filter(history -> history.theme().id().equals(selectedThemeId))
                .filter(history -> history.time().id().equals(timeId))
                .map(MyReservationHistoryResponse::waitingId)
                .findFirst()
                .orElse(null);
    }

    private List<MyReservationHistoryResponse> getMyHistories(final String reservationName, final String errorCode) {
        if (reservationName == null || reservationName.isBlank()) {
            return List.of();
        }

        if (ErrorCode.INVALID_INPUT.getCode().equals(errorCode)) {
            return List.of();
        }

        return myReservationHistoryService.getAllByName(reservationName).stream()
                .map(MyReservationHistoryResponse::from)
                .toList();
    }
}
