package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMemberId;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationDeleteService;
import roomescape.service.reservation.ReservationReadService;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationDeleteService reservationDeleteService;
    private final ReservationCreateService reservationCreateService;
    private final ReservationReadService reservationReadService;

    public ReservationController(ReservationDeleteService reservationDeleteService, ReservationCreateService reservationCreateService, ReservationReadService reservationReadService) {
        this.reservationDeleteService = reservationDeleteService;
        this.reservationCreateService = reservationCreateService;
        this.reservationReadService = reservationReadService;
    }

    @GetMapping
    public List<ReservationResponse> findAll() {
        return reservationReadService.findAll();
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody @Valid ReservationRequest reservationRequest,
            @LoginMemberId long memberId) {
        ReservationResponse reservationResponse = reservationCreateService.createMemberReservation(reservationRequest, memberId);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") long reservationId) {
        reservationDeleteService.deleteById(reservationId);
        return ResponseEntity.noContent().build();
    }
}
