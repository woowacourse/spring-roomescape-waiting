package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.ReservationSaveRequest;
import roomescape.reservation.controller.dto.ReservationSaveResponse;
import roomescape.reservation.controller.dto.ReservationWithWaitingOrderResponse;
import roomescape.reservation.controller.dto.UserReservationUpdateRequest;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationWithWaitingOrderResponse>> getAllByName(@RequestParam String name) {
        List<ReservationWithWaitingOrderResponse> body = reservationService.getAllByName(name);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @RequestParam String name) {
        reservationService.cancelForUser(id, name);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable Long id, @RequestBody @Valid UserReservationUpdateRequest request) {
        ReservationResponse body = ReservationResponse.from(
                reservationService.update(id, request.timeId()));
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<ReservationSaveResponse> create(
            @RequestBody @Valid ReservationSaveRequest reservationRequest) {
        ReservationSaveResponse body = ReservationSaveResponse.from(
                reservationService.create(reservationRequest.toServiceDto()));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
