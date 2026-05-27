package roomescape.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.controller.dto.PageRequest;
import roomescape.reservation.controller.dto.ReservationListResponse;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {
    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<ReservationListResponse> getAllReservations(@ModelAttribute @Valid PageRequest pageRequest) {
        return ResponseEntity.ok(
                ReservationListResponse.from(
                        reservationService.findAllReservations(pageRequest.page(), pageRequest.size())
                                .stream()
                                .map(ReservationResponse::from)
                                .toList()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
