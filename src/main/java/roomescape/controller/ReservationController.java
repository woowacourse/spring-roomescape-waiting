package roomescape.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.dto.WaitingRequest;
import roomescape.dto.ReservationResponse;
import roomescape.dto.WaitingResponse;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;

@RequestMapping("/reservations")
@RestController
@Validated
public class ReservationController {
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ReservationResponse response = reservationService.save(now, request);
        URI location = URI.create("/reservations/" + response.id());
        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(
            @PathVariable long id
    ) {
        return reservationService.findById(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<ReservationResponse> getReservations(@RequestParam String username) {
        return reservationService.findAllByName(username);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{reservationId}")
    public void updateReservation(
            @PathVariable long reservationId,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        LocalDateTime now = LocalDateTime.now();
        reservationService.update(reservationId, now, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id) {
        LocalDateTime now = LocalDateTime.now();
        reservationService.delete(now, id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/waitings/{id}")
    public void deleteWaiting(@PathVariable Long id) {
        LocalDateTime now = LocalDateTime.now();
        waitingService.delete(now, id);
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> createWaiting(@Valid @RequestBody WaitingRequest request) {
        LocalDateTime now = LocalDateTime.now();
        WaitingResponse response = waitingService.save(now, request);
        URI location = URI.create("/waiting/" + response.id());
        return ResponseEntity
                .created(location)
                .body(response);
    }
}
