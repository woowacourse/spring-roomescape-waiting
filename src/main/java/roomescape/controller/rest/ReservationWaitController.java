package roomescape.controller.rest;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.dto.SessionMember;
import roomescape.service.ReservationWaitService;
import roomescape.service.request.CreateReservationWaitRequest;
import roomescape.service.response.ReservationResponse;
import roomescape.service.response.ReservationWaitResponse;

@RestController
@RequestMapping("/reservations/waits")
public class ReservationWaitController {

    private final ReservationWaitService reservationWaitService;

    public ReservationWaitController(final ReservationWaitService reservationWaitService) {
        this.reservationWaitService = reservationWaitService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitResponse> createReservationWait(
            @RequestBody @Valid CreateReservationWaitRequest request, final SessionMember sessionMember) {
        ReservationWaitResponse response = reservationWaitService.createReservationWait(request, sessionMember.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}")
    public ResponseEntity<ReservationResponse> approveReservationWait(@PathVariable("id") Long waitId) {
        ReservationResponse response = reservationWaitService.approveReservationWait(waitId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationWait(@PathVariable("id") Long waitId) {
        reservationWaitService.deleteReservationWait(waitId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ReservationWaitResponse>> getAllReservation() {
        final List<ReservationWaitResponse> response = reservationWaitService.getAllWaitReservation();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationWaitResponse>> getMyReservation(final SessionMember sessionMember) {
        final List<ReservationWaitResponse> response = reservationWaitService.findAllMyWaitReservation(
                sessionMember.id());
        return ResponseEntity.ok(response);
    }
}
