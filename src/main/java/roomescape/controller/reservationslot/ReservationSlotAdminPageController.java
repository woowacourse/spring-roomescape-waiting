package roomescape.controller.reservationslot;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.reservationslot.dto.ReservationSlotResponse;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.exception.ApiException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.reservationslot.ReservationSlotService;
import roomescape.service.theme.ThemeService;

@Controller
@RequestMapping("/pages/admin/reservation-slots")
public class ReservationSlotAdminPageController {
    private final ReservationSlotService reservationSlotService;
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;

    public ReservationSlotAdminPageController(
            final ReservationSlotService reservationSlotService,
            final ThemeService themeService,
            final ReservationTimeService reservationTimeService
    ) {
        this.reservationSlotService = reservationSlotService;
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public String getReservationSlotAdminPage(
            @RequestParam(required = false) final String errorCode,
            final Model model
    ) {
        model.addAttribute("slots", reservationSlotService.getAll().stream()
                .map(ReservationSlotResponse::from)
                .toList());
        model.addAttribute("themes", themeService.getAll().stream()
                .map(ThemeResponse::from)
                .toList());
        model.addAttribute("reservationTimes", reservationTimeService.getAll().stream()
                .map(ReservationTimeResponse::from)
                .toList());
        model.addAttribute("errorCode", errorCode);
        return "reservationslot/list";
    }

    @PostMapping
    public String openReservationSlot(
            @RequestParam(required = false) final String date,
            @RequestParam(required = false) final Long themeId,
            @RequestParam(required = false) final Long timeId,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            validateReferenceIds(themeId, timeId);
            reservationSlotService.open(parseDate(date), themeId, timeId);
        } catch (ApiException exception) {
            redirectAttributes.addAttribute("errorCode", exception.getCode());
            return "redirect:/pages/admin/reservation-slots";
        } catch (Exception exception) {
            redirectAttributes.addAttribute("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
            return "redirect:/pages/admin/reservation-slots";
        }

        return "redirect:/pages/admin/reservation-slots";
    }

    private void validateReferenceIds(final Long themeId, final Long timeId) {
        if (themeId == null || timeId == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "테마와 예약 시간은 필수입니다.");
        }
    }

    private LocalDate parseDate(final String date) {
        if (date == null || date.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException exception) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "날짜 형식이 올바르지 않습니다. yyyy-MM-dd 형식이어야 합니다."
            );
        }
    }
}
