package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.request.AdminReservationCreateRequest;
import roomescape.reservation.dto.response.ReservationGetResponse;
import roomescape.reservation.service.ReservationService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    private AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private ReservationGetResponse createAdminReservation(@RequestBody @Valid AdminReservationCreateRequest requestBody) {
        return ReservationGetResponse.from(reservationService.createReservation(requestBody));
    }

    @GetMapping
    private List<ReservationGetResponse> readFilteredReservation(@RequestParam(value = "memberId", required = false) Long memberId,
                                                                 @RequestParam(value = "themeId", required = false) Long themeId,
                                                                 @RequestParam(value = "startDate", required = false) LocalDate startDate,
                                                                 @RequestParam(value = "endDate", required = false) LocalDate endDate) {
        return reservationService.findReservationByMemberIdAndThemeIdAndStartDateAndEndDate(memberId, themeId, startDate, endDate).stream()
                .map(ReservationGetResponse::from)
                .toList();
    }
}
