package roomescape.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.ReservationStatusResponse;
import roomescape.service.ReservationLookupService;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/admin/reservation-statuses")
public class AdminReservationStatusController {

    private final ReservationLookupService reservationLookupService;

    public AdminReservationStatusController(ReservationLookupService reservationLookupService) {
        this.reservationLookupService = reservationLookupService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationStatusResponse>> getReservationStatuses(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    ) {
        List<ReservationStatusResponse> results = reservationLookupService.findByDateRange(startDate, endDate)
                .stream().map(ReservationStatusResponse::from).toList();
        return ResponseEntity.ok(results);
    }
}
