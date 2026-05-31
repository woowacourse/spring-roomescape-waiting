package roomescape.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.reservation.response.ReservationResponses;
import roomescape.infrastructure.LoginUserId;
import roomescape.service.ReservationService;

@Validated
@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<ReservationResponses> readReservations(
            @LoginUserId Long managerId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Size(min = 1) String name
    ) {
        return ResponseEntity.ok(reservationService.getReservations(page, size, name, managerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@LoginUserId Long managerId, @PathVariable Long id) {
        reservationService.deleteReservation(id, managerId);
        return ResponseEntity.ok().build();
    }
}
