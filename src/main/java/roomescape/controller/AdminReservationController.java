package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.admin.AdminReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.search.SearchConditionsRequest;
import roomescape.service.reservation.ReservationService;

@RestController
@RequestMapping("admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addAdminReservation(
            @Valid @RequestBody AdminReservationRequest adminReservationRequest) {

        return ResponseEntity.created(URI.create("admin/reservations"))
                .body(reservationService.createByAdmin(adminReservationRequest));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponse>> findReservationByConditions(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        SearchConditionsRequest searchConditionsRequest = new SearchConditionsRequest(themeId, memberId, dateFrom, dateTo);

        return ResponseEntity.ok()
                .body(reservationService.getReservationsByConditions(searchConditionsRequest));
    }
}
