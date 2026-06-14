package roomescape.controller.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import roomescape.controller.history.dto.HistoryResponse;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.reservationtime.dto.ReservationTimeSlotResponse;
import roomescape.controller.reservationtime.dto.ReservationTimeSlotStatus;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.exception.ErrorCode;
import roomescape.service.history.MyHistoryService;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;

@Component
public class ReservationPageModelAssembler {
    private static final String WAITING_STATUS = "WAITING";

    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MyHistoryService myHistoryService;

    public ReservationPageModelAssembler(
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService,
            final MyHistoryService myHistoryService
    ) {
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.myHistoryService = myHistoryService;
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
            final String errorCode,
            final LocalDateTime requestedAt
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
        List<HistoryResponse> myHistories = getMyHistories(reservationName, errorCode);
        model.addAttribute("availableTimes", getAvailableTimes(
                selectedThemeId,
                selectedTheme,
                selectedDate,
                myHistories,
                requestedAt
        ));
        model.addAttribute("myHistories", myHistories);
    }

    private List<ReservationTimeSlotResponse> getAvailableTimes(
            final Long selectedThemeId,
            final ThemeResponse selectedTheme,
            final LocalDate selectedDate,
            final List<HistoryResponse> myHistories,
            final LocalDateTime requestedAt
    ) {
        if (selectedTheme == null || selectedDate == null) {
            return List.of();
        }

        Set<Long> availableTimeIds = reservationTimeService.findAvailableTimes(selectedDate, selectedThemeId, requestedAt)
                .stream()
                .map(reservationTime -> reservationTime.getId())
                .collect(Collectors.toSet());

        return reservationTimeService.getAll().stream()
                .map(reservationTime -> new ReservationTimeSlotResponse(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        resolveSlotStatus(
                                selectedDate,
                                reservationTime.getStartAt(),
                                availableTimeIds.contains(reservationTime.getId()),
                                requestedAt
                        ),
                        findWaitingId(myHistories, selectedDate, selectedThemeId, reservationTime.getId())
                ))
                .toList();
    }

    private ReservationTimeSlotStatus resolveSlotStatus(
            final LocalDate selectedDate,
            final LocalTime startAt,
            final boolean reservable,
            final LocalDateTime requestedAt
    ) {
        if (LocalDateTime.of(selectedDate, startAt).isBefore(requestedAt)) {
            return ReservationTimeSlotStatus.PAST;
        }

        if (reservable) {
            return ReservationTimeSlotStatus.RESERVABLE;
        }

        return ReservationTimeSlotStatus.WAITABLE;
    }

    private Long findWaitingId(
            final List<HistoryResponse> myHistories,
            final LocalDate selectedDate,
            final Long selectedThemeId,
            final Long timeId
    ) {
        return myHistories.stream()
                .filter(history -> WAITING_STATUS.equals(history.status()))
                .filter(history -> history.date().equals(selectedDate))
                .filter(history -> history.theme().id().equals(selectedThemeId))
                .filter(history -> history.time().id().equals(timeId))
                .map(HistoryResponse::waitingId)
                .findFirst()
                .orElse(null);
    }

    private List<HistoryResponse> getMyHistories(final String reservationName, final String errorCode) {
        if (reservationName == null || reservationName.isBlank()) {
            return List.of();
        }

        if (ErrorCode.INVALID_INPUT.getCode().equals(errorCode)) {
            return List.of();
        }

        return myHistoryService.getAllByName(reservationName).stream()
                .map(HistoryResponse::from)
                .toList();
    }
}
