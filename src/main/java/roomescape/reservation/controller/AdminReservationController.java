package roomescape.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.common.dto.PageResult;
import roomescape.reservation.controller.dto.PageRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {
    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<PageResult<ReservationResponse>> getAllReservations(@ModelAttribute @Valid PageRequest pageRequest) {
        return ResponseEntity.ok(
                reservationService.findAllReservations(pageRequest.page(), pageRequest.size())
                        .map(ReservationResponse::from));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
