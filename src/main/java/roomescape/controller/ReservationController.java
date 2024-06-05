package roomescape.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.config.Authorization;
import roomescape.dto.MyReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.service.ReservationService;
import roomescape.service.ReservationWaitingService;

@RequestMapping("/reservations")
@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    public ReservationController(ReservationService reservationService,
            ReservationWaitingService reservationWaitingService
    ) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<MyReservationResponse>> findMyReservationsAndWaitings(@Authorization long memberId) {
        List<MyReservationResponse> reservations = reservationService.findReservationsByMemberId(memberId);
        List<MyReservationResponse> waitings = reservationWaitingService.findWaitingsByMemberId(memberId);
        List<MyReservationResponse> response = new ArrayList<>(reservations);
        response.addAll(waitings);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @Authorization long memberId,
            @RequestBody ReservationRequest request
    ) {
        ReservationRequest requestWithMemberId = new ReservationRequest(memberId, request.date(), request.timeId(),
                request.themeId());
        ReservationResponse response = reservationService.reserve(requestWithMemberId);
        URI location = URI.create("/reservations/" + response.id());
        return ResponseEntity.created(location).body(response);
    }
}
