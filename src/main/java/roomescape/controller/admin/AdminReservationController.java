package roomescape.controller.admin;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationService;
import roomescape.service.dto.request.ReservationCreateRequest;
import roomescape.service.dto.response.ListResponse;
import roomescape.service.dto.response.ReservationResponse;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest reservationCreateRequest) {
        ReservationResponse reservationResponse = reservationService.save(reservationCreateRequest);
        return ResponseEntity.created(URI.create("/reservations" + reservationResponse.id())).body(reservationResponse);
    }

    @GetMapping
    public ResponseEntity<ListResponse<ReservationResponse>> findBy(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        return ResponseEntity.ok().body(reservationService.findBy(themeId, memberId, dateFrom, dateTo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/waiting")
    public ResponseEntity<ListResponse<ReservationResponse>> findAllWaiting() {
        return ResponseEntity.ok().body(reservationService.findAllWaiting());
    }

    @PostMapping("/waiting/{id}/approve")
    public ResponseEntity<Void> approveWaiting(@PathVariable Long id) {
        reservationService.approveWaiting(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/waiting/{id}/deny")
    public ResponseEntity<Void> denyWaiting(@PathVariable Long id) {
        reservationService.cancelWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
