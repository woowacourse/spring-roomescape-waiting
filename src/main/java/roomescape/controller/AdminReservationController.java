package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationCommonService;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationFilterRequest;
import roomescape.service.reservation.dto.ReservationResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationCreateService reservationCreateService;
    private final ReservationCommonService reservationCommonService;

    public AdminReservationController(ReservationCreateService reservationCreateService, ReservationCommonService reservationCommonService) {
        this.reservationCreateService = reservationCreateService;
        this.reservationCommonService = reservationCommonService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody @Valid AdminReservationRequest adminReservationRequest) {
        ReservationResponse reservationResponse = reservationCreateService.createAdminReservation(adminReservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/search")
    public List<ReservationResponse> findReservations(
            @ModelAttribute("ReservationFindRequest") ReservationFilterRequest reservationFilterRequest) {
        return reservationCommonService.findByCondition(reservationFilterRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") long reservationId) {
        reservationCommonService.deleteById(reservationId);
        return ResponseEntity.noContent().build();
    }
}
