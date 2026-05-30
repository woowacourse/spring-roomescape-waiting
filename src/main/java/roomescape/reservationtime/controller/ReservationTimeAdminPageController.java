package roomescape.reservationtime.controller;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.reservationtime.service.ReservationTimeService;

@Controller
@RequestMapping("/pages/admin/reservation-times")
public class ReservationTimeAdminPageController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeAdminPageController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public String getReservationTimeAdminPage(
            @RequestParam(required = false) final String errorCode,
            final Model model
    ) {
        model.addAttribute("reservationTimes", reservationTimeService.getAll().stream()
                .map(ReservationTimeResponse::from)
                .toList());
        model.addAttribute("errorCode", errorCode);
        return "reservationtime/list";
    }

    @PostMapping
    public String createReservationTime(@RequestParam(required = false) final String startAt) {
        reservationTimeService.save(parseTime(startAt));
        return "redirect:/pages/admin/reservation-times";
    }

    @PostMapping("/{timeId}/delete")
    public String deleteReservationTime(@PathVariable final Long timeId) {
        reservationTimeService.deleteById(timeId);
        return "redirect:/pages/admin/reservation-times";
    }

    private LocalTime parseTime(final String startAt) {
        if (startAt == null || startAt.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(startAt);
        } catch (DateTimeParseException exception) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "시간 형식이 올바르지 않습니다. HH:mm 형식이어야 합니다."
            );
        }
    }
}
