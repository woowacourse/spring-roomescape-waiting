package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservationOrder.OrderResponse;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.service.ReservationOrderService;
import roomescape.service.ReservationService;

import java.util.List;
import roomescape.service.ReservationWaitingService;

@RestController
public class ReservationRestController {

    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;
    private final ReservationOrderService reservationOrderService;

    public ReservationRestController(ReservationService reservationService,
                                     ReservationWaitingService reservationWaitingService,
                                     ReservationOrderService reservationOrderService) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
        this.reservationOrderService = reservationOrderService;
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> readAll() {
        return reservationService.readAll();
    }

    @GetMapping("/reservations/mine")
    public List<ReservationResponse> readMine(@RequestParam String name) {
        return reservationService.readByName(name);
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> read(@PathVariable Long id) {
        ReservationResponse reservationResponse = reservationService.read(id);
        return ResponseEntity.ok(reservationResponse);
    }

    @GetMapping("/reservations/{reservationId}/order")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable long reservationId) {
        OrderResponse orderResponse = OrderResponse.from(reservationOrderService.getByReservationId(reservationId));
        return ResponseEntity.ok(orderResponse);
    }

    @PostMapping("/reservations")
    public ResponseEntity<OrderResponse> create(@RequestBody ReservationRequest reservationReq) {
        OrderResponse newReservation = reservationService.reserve(reservationReq);
        return new ResponseEntity<>(newReservation, HttpStatus.CREATED);
    }

    @PutMapping("/reservations/{id}")
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

    @GetMapping("/reservations/waitings/mine")
    public List<ReservationWaitingResponse> waitingReadMine(@RequestParam String name) {
        return reservationWaitingService.readByName(name);
    }
}
