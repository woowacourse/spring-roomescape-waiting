package roomescape.domain.reservation.controller;

import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.dto.response.ReservationTimeStartAtResponseDto;
import roomescape.domain.reservation.service.ReservationService;

@RestController
@RequestMapping("/api/admin/reservations")
@Validated
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> getReservations() {
        return ResponseEntity.ok(reservationService.getReservations());
    }

    @GetMapping("/{id}/time-start-at")
    public ResponseEntity<ReservationTimeStartAtResponseDto> getReservationTimeStartAt(
        @PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id
    ) {
        return ResponseEntity.ok(reservationService.getReservationTimeStartAtForSqlObservation(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
