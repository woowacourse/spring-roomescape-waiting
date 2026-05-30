package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.UpdateMyReservation;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationFacade;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationFacade reservationFacade;

    public ReservationController(ReservationFacade reservationFacade) {
        this.reservationFacade = reservationFacade;
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponse> create(@RequestBody ReservationRequest request) {
        ReservationCreateResponse reservationCreateResponse = reservationFacade.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationCreateResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll() {
        return ResponseEntity.ok(reservationFacade.findAllReservation());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationFacade.findReservationById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationFacade.deleteReservation(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<MyReservationResponse>> findByName(@RequestParam String name) {
        return ResponseEntity.ok(reservationFacade.findReservationsByName(name));
    }

    @DeleteMapping("/my")
    public ResponseEntity<Void> deleteMyReservation(@RequestParam String name,
        @RequestParam Long reservationId) {
        reservationFacade.deleteReservedByNameAndReservationId(name, reservationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/my/waiting")
    public ResponseEntity<Void> deleteMyWaiting(@RequestParam String name,
        @RequestParam Long reservationId) {
        reservationFacade.deleteWaitingByNameAndReservationId(name, reservationId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping
    public ResponseEntity<Void> updateMyReservation(
        @RequestBody UpdateMyReservation updateMyReservation,
        @RequestParam String name,
        @RequestParam Long reservationId) {
        reservationFacade.updateMyReservation(updateMyReservation, name, reservationId);
        return ResponseEntity.ok().build();
    }
}
