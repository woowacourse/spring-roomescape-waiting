package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.reservation.MyReservationResponse;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.service.ReservationService;

import java.util.List;
import roomescape.service.ReservationWaitingService;

@RestController
public class ReservationRestController {

    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    public ReservationRestController(ReservationService reservationService,
                                     ReservationWaitingService reservationWaitingService) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> readAll() {
        return reservationService.readAll();
    }

    @GetMapping("/reservations/mine")
    public List<MyReservationResponse> readMine(@RequestParam String name) {
        return reservationService.readMineByName(name);
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> read(@PathVariable Long id) {
        ReservationResponse reservationResponse = reservationService.read(id);
        return ResponseEntity.ok(reservationResponse);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(@RequestBody ReservationRequest reservationReq) {
        ReservationResponse newReservation = reservationService.create(reservationReq);
        return new ResponseEntity<>(newReservation, HttpStatus.CREATED);
    }

    @PatchMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> update(@PathVariable Long id,
                                                      @RequestBody ReservationRequest reservationReq) {
        ReservationResponse updatedReservation = reservationService.update(id, reservationReq);
        return ResponseEntity.ok(updatedReservation);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reservations/waitings")
    public ResponseEntity<ReservationWaitingResponse> waitingCreate(
            @RequestBody ReservationWaitingRequest reservationWaitingReq) {
        ReservationWaitingResponse reservationWaitingResponse = reservationWaitingService.create(reservationWaitingReq);
        return new ResponseEntity<>(reservationWaitingResponse, HttpStatus.CREATED);
    }

    @DeleteMapping("/reservations/waitings/{id}")
    public ResponseEntity<Void> waitingDelete(@PathVariable Long id) {
        reservationWaitingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations/waitings")
    public List<ReservationWaitingResponse> waitingReadAll() {
        return reservationWaitingService.readAll();
    }
}
