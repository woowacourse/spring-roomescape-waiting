package roomescape.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationWaitingResponse;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.service.ReservationService;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<ResponsesWrapper<ReservationResponse>> getReservations() {
        return ResponseEntity.ok(reservationService.findEntireReservations());
    }

    @GetMapping("waiting")
    public ResponseEntity<ResponsesWrapper<ReservationWaitingResponse>> getWaitingReservations() {
        return ResponseEntity.ok(reservationService.findEntireWaitingReservations());
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> register(@RequestBody ReservationRequest reservationRequest) {
        ReservationResponse reservationResponse = reservationService.register(reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id())).body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<ResponsesWrapper<ReservationResponse>> search(@RequestParam Long themeId, @RequestParam Long memberId, @RequestParam LocalDate dateFrom, @RequestParam LocalDate dateTo) {
        return ResponseEntity.ok(reservationService.findReservations(themeId, memberId, dateFrom, dateTo));
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        reservationService.deleteWaitingById(id);
        return ResponseEntity.noContent().build();
    }
}
