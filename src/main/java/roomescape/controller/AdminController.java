package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.request.AdminReservationRequest;
import roomescape.controller.response.ReservationResponse;
import roomescape.controller.response.WaitingResponse;
import roomescape.model.Reservation;
import roomescape.model.Waiting;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;
import roomescape.service.dto.ReservationDto;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public AdminController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addReservation(@Valid @RequestBody AdminReservationRequest request) {
        ReservationDto reservationDto = request.toDto();
        Reservation reservation = reservationService.saveReservation(reservationDto);
        ReservationResponse response = ReservationResponse.from(reservation);
        return ResponseEntity
                .created(URI.create("/admin/reservations/" + response.getId()))
                .body(response);
    }

    @GetMapping("/reservations/waiting")
    public ResponseEntity<List<WaitingResponse>> getAllWaiting() {
        List<Waiting> allWaiting = waitingService.findAllWaiting();
        List<WaitingResponse> response = allWaiting.stream()
                .map(WaitingResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reservations/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@NotNull @Min(1) @PathVariable("id") Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
