package roomescape.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationResponses;
import roomescape.service.ReservationService;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin")
public class AdminReservationController {
    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<ReservationResponses> read() {
        return ResponseEntity.ok(reservationService.findEntireReservationList());
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<ReservationResponses> search(@RequestParam Long themeId, @RequestParam Long memberId, @RequestParam LocalDate dateFrom, @RequestParam LocalDate dateTo) {
        return ResponseEntity.ok(reservationService.findReservations(themeId, memberId, dateFrom, dateTo));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(@RequestBody ReservationRequest reservationRequest) {
        ReservationResponse reservationResponse = reservationService.create(reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id())).body(reservationResponse);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
