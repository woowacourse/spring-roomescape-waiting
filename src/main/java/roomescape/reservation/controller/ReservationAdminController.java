package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.service.ReservationQueryService;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
public class ReservationAdminController {

    private final ReservationService reservationService;
    private final ReservationQueryService reservationQueryService;

    public ReservationAdminController(ReservationService reservationService,
                                      ReservationQueryService reservationQueryService) {
        this.reservationService = reservationService;
        this.reservationQueryService = reservationQueryService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> responses = reservationQueryService.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable Long id) {
        reservationService.deleteByAdmin(id, LocalDateTime.now());
    }
}
