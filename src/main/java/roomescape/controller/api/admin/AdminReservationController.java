package roomescape.controller.api.admin;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.AdminReservationCreateRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationService;

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
    private ReservationResponse createAdminReservation(@RequestBody AdminReservationCreateRequest adminReservationCreateRequest) {
        return ReservationResponse.from(reservationService.createReservation(adminReservationCreateRequest));
    }

    @GetMapping
    private List<ReservationResponse> readFilteredReservation(@RequestParam("memberId") @Nullable Long memberId,
                                                              @RequestParam("themeId") @Nullable Long themeId,
                                                              @RequestParam("startDate") @Nullable LocalDate startDate,
                                                              @RequestParam("endDate") @Nullable LocalDate endDate) {
        return reservationService.findReservationByMemberIdAndThemeIdAndStartDateAndEndDate(memberId, themeId, startDate, endDate).stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
