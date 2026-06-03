package roomescape.feature.reservation.controller;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.reservation.service.AdminReservationService;

@RestController
@RequestMapping("/api/admin/reservations")
@Validated
@RequiredArgsConstructor
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> getReservations() {
        return ResponseEntity.ok(adminReservationService.getReservations());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id) {
        adminReservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
