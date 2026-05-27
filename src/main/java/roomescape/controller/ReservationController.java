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
import roomescape.dto.ReservationResponse;
import roomescape.service.ReservationService;

@RequestMapping("/reservations")
@RestController
@Validated
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
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

//    @GetMapping("/{id}")
//    public ReservationResponse getReservation(
//            @PathVariable long id
//    ) {
//        return waitingService.findById(id);
//    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<ReservationResponse> getReservations(@RequestParam String username) {
        return reservationService.findAllByName(username);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{reservationId}")
    public void updateReservation(
            @PathVariable long reservationId,
            @Valid @RequestBody ReservationRequest request
    ) {
        LocalDateTime now = LocalDateTime.now();
        reservationService.update(reservationId, now, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{reservationId}")
    public void deleteReservation(@PathVariable Long reservationId) {
        LocalDateTime now = LocalDateTime.now();
        reservationService.delete(now, reservationId);
    }

}
