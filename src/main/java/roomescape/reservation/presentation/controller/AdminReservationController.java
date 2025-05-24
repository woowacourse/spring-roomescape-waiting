package roomescape.reservation.presentation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.AdminWaitingReservationResponse;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.SearchConditionsRequest;

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

        SearchConditionsRequest searchConditionsRequest = new SearchConditionsRequest(themeId, memberId, dateFrom,
                dateTo);

        return ResponseEntity.ok()
                .body(reservationService.getReservationsByConditions(searchConditionsRequest));
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<AdminWaitingReservationResponse>> getWaitingReservations() {
        return ResponseEntity.ok().body(reservationService.getWaitingReservations());
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> denyWaitingReservation(@PathVariable Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
