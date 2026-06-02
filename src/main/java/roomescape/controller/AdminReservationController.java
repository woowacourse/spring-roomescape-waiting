package roomescape.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.User;
import roomescape.dto.reservation.response.ReservationResponses;
import roomescape.infrastructure.AdminOnly;
import roomescape.infrastructure.LoginUser;
import roomescape.service.ReservationService;

@Validated
@RestController
@RequestMapping("/admin/reservations")
@AdminOnly
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<ReservationResponses> readReservations(
            @LoginUser User manager,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Size(min = 1) String name
    ) {
        return ResponseEntity.ok(reservationService.getReservations(page, size, name, manager));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@LoginUser User manager, @PathVariable Long id) {
        reservationService.cancelReservation(id, manager);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePastReservation(@LoginUser User manager, @PathVariable Long id) {
        reservationService.deletePastReservation(id, manager);
        return ResponseEntity.ok().build();
    }
}
