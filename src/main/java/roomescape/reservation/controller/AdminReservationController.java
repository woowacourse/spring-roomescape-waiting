package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.service.ReservationService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ReservationResponse createReservation(@Valid @RequestBody ReservationCreateRequest request) {
        return reservationService.createReservation(request);
    }

    @PostMapping("/waitings/{id}")
    public ReservationResponse approveWaiting(@PathVariable Long id) {
        return reservationService.approveWaiting(id);
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> readReservations() {
        return reservationService.readReservations();
    }

    @GetMapping("/reservations/search")
    public List<ReservationResponse> readReservations(
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo,
            @RequestParam Long memberId,
            @RequestParam Long themeId
    ) {
        return reservationService.searchReservations(dateFrom, dateTo, memberId, themeId);
    }

    @GetMapping("/waitings")
    public List<WaitingResponse> readWaitings() {
        return reservationService.readWaitings();
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> rejectWaiting(@PathVariable Long id) {
        reservationService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
