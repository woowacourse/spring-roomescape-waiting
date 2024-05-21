package roomescape.web.controller;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import roomescape.service.ReservationService;
import roomescape.web.dto.request.reservation.ReservationRequest;
import roomescape.web.dto.response.reservation.ReservationResponse;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.saveReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }
}

