package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMemberId;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationDeleteService;
import roomescape.service.reservation.ReservationReadService;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationFilterRequest;
import roomescape.service.reservation.dto.ReservationResponse;
import roomescape.service.theme.ThemeService;
import roomescape.service.theme.dto.ThemeRequest;
import roomescape.service.theme.dto.ThemeResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationCreateService reservationCreateService;
    private final ReservationReadService reservationReadService;
    private final ReservationDeleteService reservationDeleteService;

    public AdminReservationController(ReservationCreateService reservationCreateService, ReservationReadService reservationReadService, ReservationDeleteService reservationDeleteService) {
        this.reservationCreateService = reservationCreateService;
        this.reservationReadService = reservationReadService;
        this.reservationDeleteService = reservationDeleteService;
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
        return reservationReadService.findByCondition(reservationFilterRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") long reservationId) {
        reservationDeleteService.deleteById(reservationId);
        return ResponseEntity.noContent().build();
    }
}
