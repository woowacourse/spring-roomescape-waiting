package roomescape.ui.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import roomescape.holiday.controller.dto.HolidayResponse;
import roomescape.holiday.exception.HolidayNotFoundException;
import roomescape.holiday.service.HolidayService;
import roomescape.holiday.service.dto.HolidaySaveServiceDto;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.ForbiddenRequestException;
import roomescape.reservation.exception.PastReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationSaveServiceRequest;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeSaveServiceRequest;
import roomescape.time.controller.dto.TimeResponse;
import roomescape.time.exception.ReservationTimeConflictException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.service.TimeService;

@Controller
@RequestMapping("/page")
public class RoomescapePageController {

    private static final Logger log = LoggerFactory.getLogger(RoomescapePageController.class);

    private final ReservationService reservationService;
    private final ThemeService themeService;
    private final TimeService timeService;
    private final HolidayService holidayService;
    private final String clientKey;

    public RoomescapePageController(
            ReservationService reservationService,
            ThemeService themeService,
            TimeService timeService,
            HolidayService holidayService,
            @Value("${toss.client-key}") String clientKey
    ) {
        this.reservationService = reservationService;
        this.themeService = themeService;
        this.timeService = timeService;
        this.holidayService = holidayService;
        this.clientKey = clientKey;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/index";
    }

    @GetMapping("/dashboard/reservations")
    public String reservationsPage(Model model) {
        model.addAttribute("reservations",
                reservationService.getAll().stream().map(ReservationResponse::from).toList());
        model.addAttribute("themes", themeService.getAll().stream().map(ThemeResponse::from).toList());
        model.addAttribute("times", timeService.findAll().stream().map(TimeResponse::from).toList());
        return "dashboard/reservations";
    }

    @GetMapping("/dashboard/availability")
    public String availabilityPage(
            @RequestParam(required = false) Long availableThemeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableDate,
            Model model
    ) {
        model.addAttribute("themes", themeService.getAll().stream().map(ThemeResponse::from).toList());
        model.addAttribute("selectedAvailableThemeId", availableThemeId);
        model.addAttribute("selectedDate", availableDate);
        model.addAttribute("availableTimes", availableTimes(availableThemeId, availableDate, model));
        return "dashboard/availability";
    }

    @GetMapping("/dashboard/themes")
    public String themesPage(Model model) {
        model.addAttribute("themes", themeService.getAll().stream().map(ThemeResponse::from).toList());
        model.addAttribute("bestThemes", themeService.getBestThemes().stream().map(ThemeResponse::from).toList());
        return "dashboard/themes";
    }

    @GetMapping("/dashboard/times")
    public String timesPage(Model model) {
        model.addAttribute("times", timeService.findAll().stream().map(TimeResponse::from).toList());
        return "dashboard/times";
    }

    @GetMapping("/dashboard/holidays")
    public String holidaysPage(Model model) {
        model.addAttribute("holidays", holidayService.getAll().stream().map(HolidayResponse::from).toList());
        return "dashboard/holidays";
    }

    @PostMapping("/dashboard/reservations")
    public String createReservation(
            @RequestParam String name,
            @RequestParam Long themeId,
            @RequestParam Long timeId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            reservationService.create(new ReservationSaveServiceRequest(name, themeId, timeId, null, null));
            addSuccessMessage(redirectAttributes, "예약을 생성했습니다.");
        } catch (PastReservationException | DuplicateReservationException |
                 IllegalArgumentException | ThemeNotFoundException | TimeNotFoundException e) {
            addExpectedErrorMessage(redirectAttributes, "예약 생성에 실패했습니다. 입력값을 다시 확인해 주세요.", e);
        }
        return "redirect:/page/dashboard/reservations";
    }

    @GetMapping("/reservations")
    public String userReservationsPage(@RequestParam(required = false) String name, Model model) {
        model.addAttribute("themes", themeService.getAll().stream().map(ThemeResponse::from).toList());
        model.addAttribute("times", timeService.findAll().stream().map(TimeResponse::from).toList());
        if (name != null && !name.isBlank()) {
            model.addAttribute("reservations", reservationService.getAllByName(name));
            model.addAttribute("name", name);
        }
        return "reservations";
    }

    @PostMapping("/reservations")
    public String createUserReservation(
            @RequestParam String name,
            @RequestParam Long themeId,
            @RequestParam Long timeId,
            @RequestParam Long amount,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
            Reservation created = reservationService.create(new ReservationSaveServiceRequest(name, themeId, timeId, orderId, amount));
            if (created.getStatus() == Status.WAITING) {
                addSuccessMessage(redirectAttributes, "이미 예약된 슬롯이라 예약 대기로 등록되었습니다.");
                return "redirect:/page/reservations?name=" + name;
            }
            redirectAttributes.addAttribute("orderId", orderId);
            redirectAttributes.addAttribute("amount", amount);
            redirectAttributes.addAttribute("orderName", "방탈출 예약");
            return "redirect:/page/payment";
        } catch (PastReservationException | DuplicateReservationException |
                 IllegalArgumentException | ThemeNotFoundException | TimeNotFoundException e) {
            addExpectedErrorMessage(redirectAttributes, "예약 생성에 실패했습니다. 입력값을 다시 확인해 주세요.", e);
            return "redirect:/page/reservations?name=" + name;
        }
    }

    @GetMapping("/payment")
    public String paymentPage(
            @RequestParam String orderId,
            @RequestParam Long amount,
            @RequestParam String orderName,
            Model model
    ) {
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("orderName", orderName);
        return "payment/checkout";
    }

    @PostMapping("/reservations/{id}/update")
    public String updateUserReservation(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam Long timeId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            reservationService.update(id, timeId);
            addSuccessMessage(redirectAttributes, "예약 시간을 변경했습니다.");
        } catch (ReservationNotFoundException e) {
            addExpectedErrorMessage(redirectAttributes, "수정할 예약을 찾지 못했습니다.", e);
        } catch (PastReservationException e) {
            addExpectedErrorMessage(redirectAttributes, "이미 지난 예약은 수정할 수 없습니다.", e);
        } catch (DuplicateReservationException e) {
            addExpectedErrorMessage(redirectAttributes, "해당 시간 슬롯은 이미 예약되어 있습니다.", e);
        } catch (IllegalArgumentException | TimeNotFoundException e) {
            addExpectedErrorMessage(redirectAttributes, "예약 시간 변경에 실패했습니다. 입력값을 다시 확인해 주세요.", e);
        }
        return "redirect:/page/reservations?name=" + name;
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancelUserReservation(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) Status status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            reservationService.cancelForUser(id, name);
            if (status == Status.WAITING) {
                addSuccessMessage(redirectAttributes, "예약 대기를 취소했습니다.");
            } else {
                addSuccessMessage(redirectAttributes, "예약을 취소했습니다.");
            }
        } catch (ReservationNotFoundException e) {
            addExpectedErrorMessage(redirectAttributes, "취소할 예약을 찾지 못했습니다.", e);
        } catch (PastReservationException e) {
            addExpectedErrorMessage(redirectAttributes, "이미 지난 예약은 취소할 수 없습니다.", e);
        } catch (ForbiddenRequestException e) {
            addExpectedErrorMessage(redirectAttributes, "본인의 예약만 취소할 수 있습니다.", e);
        }
        return "redirect:/page/reservations?name=" + name;
    }

    @PostMapping("/dashboard/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancel(id);
            addSuccessMessage(redirectAttributes, "예약을 취소했습니다.");
        } catch (ReservationNotFoundException e) {
            addExpectedErrorMessage(redirectAttributes, "취소할 예약을 찾지 못했습니다.", e);
        }
        return "redirect:/page/dashboard/reservations";
    }

    @PostMapping("/dashboard/themes")
    public String createTheme(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String imageUrl,
            RedirectAttributes redirectAttributes
    ) {
        try {
            themeService.create(new ThemeSaveServiceRequest(name, description, imageUrl));
            addSuccessMessage(redirectAttributes, "테마를 생성했습니다.");
        } catch (IllegalArgumentException e) {
            addExpectedErrorMessage(redirectAttributes, "테마 생성에 실패했습니다. 입력값을 다시 확인해 주세요.", e);
        }
        return "redirect:/page/dashboard/themes";
    }

    @PostMapping("/dashboard/themes/{id}/delete")
    public String deleteTheme(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            themeService.deleteById(id);
            addSuccessMessage(redirectAttributes, "테마를 삭제했습니다.");
        } catch (ThemeNotFoundException e) {
            addExpectedErrorMessage(redirectAttributes, "삭제할 테마를 찾지 못했습니다.", e);
        }
        return "redirect:/page/dashboard/themes";
    }

    @PostMapping("/dashboard/times")
    public String createTime(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startAt,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endAt,
            RedirectAttributes redirectAttributes
    ) {
        try {
            timeService.create(startAt, endAt);
            addSuccessMessage(redirectAttributes, "시간 슬롯을 생성했습니다.");
        } catch (IllegalArgumentException e) {
            addExpectedErrorMessage(redirectAttributes, "시간 슬롯 생성에 실패했습니다. 입력값을 다시 확인해 주세요.", e);
        }
        return "redirect:/page/dashboard/times";
    }

    @PostMapping("/dashboard/times/{id}/delete")
    public String deleteTime(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            timeService.deleteById(id);
            addSuccessMessage(redirectAttributes, "시간 슬롯을 삭제했습니다.");
        } catch (ReservationTimeConflictException e) {
            addExpectedErrorMessage(redirectAttributes, "해당 시간에 예약이 존재하여 삭제할 수 없습니다.", e);
        } catch (TimeNotFoundException e) {
            addExpectedErrorMessage(redirectAttributes, "삭제할 시간 슬롯을 찾지 못했습니다.", e);
        }
        return "redirect:/page/dashboard/times";
    }

    @PostMapping("/dashboard/holidays")
    public String createHoliday(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            RedirectAttributes redirectAttributes
    ) {
        try {
            holidayService.create(new HolidaySaveServiceDto(date));
            addSuccessMessage(redirectAttributes, "휴일을 추가했습니다.");
        } catch (IllegalArgumentException e) {
            addExpectedErrorMessage(redirectAttributes, "휴일 추가에 실패했습니다. 입력값을 다시 확인해 주세요.", e);
        }
        return "redirect:/page/dashboard/holidays";
    }

    @PostMapping("/dashboard/holidays/{id}/delete")
    public String deleteHoliday(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            holidayService.delete(id);
            addSuccessMessage(redirectAttributes, "휴일을 삭제했습니다.");
        } catch (HolidayNotFoundException e) {
            addExpectedErrorMessage(redirectAttributes, "삭제할 휴일을 찾지 못했습니다.", e);
        }
        return "redirect:/page/dashboard/holidays";
    }

    private List<TimeResponse> availableTimes(Long availableThemeId, LocalDate availableDate, Model model) {
        if (availableThemeId == null || availableDate == null) {
            return List.of();
        }
        try {
            return themeService.getAvailableTimes(availableThemeId, availableDate)
                    .stream()
                    .map(TimeResponse::from)
                    .toList();
        } catch (IllegalArgumentException | ThemeNotFoundException e) {
            log.info("Failed to load available times for themeId={} date={}: {}", availableThemeId, availableDate,
                    e.getMessage());
            model.addAttribute("availableTimeErrorMessage", "예약 가능 시간을 조회하지 못했습니다. 조회 조건을 확인해 주세요.");
            return List.of();
        }
    }

    private void addSuccessMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("successMessage", message);
    }

    private void addExpectedErrorMessage(RedirectAttributes redirectAttributes, String userMessage,
                                         RuntimeException e) {
        log.info("Dashboard request failed: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", userMessage);
    }
}
