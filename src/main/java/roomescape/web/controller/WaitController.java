package roomescape.web.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import roomescape.service.ReservationService;
import roomescape.service.dto.request.member.Credential;
import roomescape.service.dto.request.reservation.ReservationRequest;
import roomescape.service.dto.request.wait.WaitRequest;
import roomescape.service.dto.response.wait.ReservationWithStatusResponse;

@RestController
@RequiredArgsConstructor
public class WaitController {
    private final ReservationService reservationService;

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationWithStatusResponse>> findAllByMemberId(Credential credential) {
        List<ReservationWithStatusResponse> responses = reservationService.findAllByMemberId(credential.memberId());

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/reservation-wait")
    public ResponseEntity<Void> saveReservationWait(@Valid @RequestBody WaitRequest request, Credential credential) {
        ReservationRequest reservationRequest = new ReservationRequest(request.date(), credential.memberId(),
                request.timeId(), request.themeId());
        reservationService.saveReservation(reservationRequest);

        return ResponseEntity.created(URI.create("/")).build();
    }

    @DeleteMapping("/reservation-wait/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("reservationId") Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}
