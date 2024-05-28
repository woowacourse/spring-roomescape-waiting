package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMemberId;
import roomescape.service.reservation.ReservationCommonService;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationCreateService reservationCreateService;
    private final ReservationCommonService reservationReadService;

    public ReservationController(ReservationCreateService reservationCreateService, ReservationCommonService reservationReadService) {
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
}
