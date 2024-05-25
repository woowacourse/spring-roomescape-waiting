package roomescape.web.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import roomescape.service.ReservationService;
import roomescape.service.ReservationWaitService;
import roomescape.service.dto.request.reservation.ReservationRequest;
import roomescape.service.dto.response.reservation.ReservationResponse;
import roomescape.service.dto.response.wait.AdminWaitResponse;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ReservationService reservationService;
    private final ReservationWaitService waitService;

    @GetMapping("/reservation-wait")
    public ResponseEntity<List<AdminWaitResponse>> getAdminReservationWait() {
        List<AdminWaitResponse> waits = waitService.findAllWaits();
        return ResponseEntity.ok(waits);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.saveReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }
}

